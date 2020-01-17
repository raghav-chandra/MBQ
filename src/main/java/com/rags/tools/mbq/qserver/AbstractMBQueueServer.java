package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.PendingQueue;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;
import com.rags.tools.mbq.util.HashingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMBQueueServer implements MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMBQueueServer.class);

    private static final Map<String, Long> CLIENTS_HB = new ConcurrentHashMap<>();
    private static final Map<Client, List<String>> CLIENTS_MESSAGES = new ConcurrentHashMap<>();

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final int PING_INTERVAL = 5000;

    private final PendingQueueMap pendingQueueMap;

    private final MBQueue queue;

    public AbstractMBQueueServer(MBQueue mbQueue, PendingQueueMap pendingQueueMap) {
        this.queue = mbQueue;
        this.pendingQueueMap = pendingQueueMap;
        init();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                List<String> allInvalids = new ArrayList<>();

                CLIENTS_HB.forEach((id, val) -> {
                    boolean isInValid = (curTime - val) > PING_INTERVAL;
                    if (isInValid) {
                        LOGGER.warn("Client with ID {} was not active {}, last heart beat received at {}", id, curTime, val);
                        allInvalids.add(id);
                    }
                });

                allInvalids.forEach(id -> {
                    CLIENTS_HB.remove(id);
                    //Move Items to the right place if client is not active
                    Optional<Client> existing = CLIENTS_MESSAGES.keySet().parallelStream().filter(c -> c.getId().equals(id)).findFirst();
                    if (existing.isPresent()) {
                        Client client = existing.get();
                        pushIdToRightPlace(getPendingQueueMap().get(client.getQueueName()), CLIENTS_MESSAGES.get(client));
                    }
                });
            }
        }, 0, 1000);
    }

    abstract void init();

    @Override
    public Client registerClient(Client client) {
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());

        if (CLIENTS_HB.containsKey(id)) {
            LOGGER.error("Client already registered with name {}, queue {} and batch {}", client.getName(), client.getQueueName(), client.getBatch());
            throw new MBQException("Client is already registered");
        }

        Client clientWithId = new Client(id, client.getName(), client.getQueueName(), client.getBatch());
        long currTime = System.currentTimeMillis();

        CLIENTS_HB.put(id, currTime);
        clientWithId.setHeartBeatId(HashingUtil.hashSHA256(id + currTime));

        return clientWithId;
    }

    private String getClientID(String workerName, String pollingQueue, int batch) {
        return HashingUtil.hashSHA256(workerName + pollingQueue + batch);
    }

    private void validateClient(Client client) {
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        if (!CLIENTS_HB.containsKey(id)) {
            LOGGER.error("Client [{}] wasn't active or dint exists", client);
            throw new MBQException("Client is not registered or wasn't active");
        }

        long curTime = System.currentTimeMillis();

        if ((curTime - CLIENTS_HB.get(id)) > PING_INTERVAL) {
            long ts = CLIENTS_HB.remove(id);
            LOGGER.error("Client [{}] was not active {}", client, ts);
            throw new MBQException("Client was existing and was not active");
        }
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        validateClient(client);

        String queueName = client.getQueueName();
        int batch = client.getBatch();

        try {
            LOCK.lock();

            PendingQueue<String> seqQ = getPendingQueueMap().get(queueName);
            if (seqQ.isEmpty()) {
                return Collections.emptyList();
            }

            int noOfItem = Math.min(seqQ.size(), batch);
            List<MBQMessage> items = new ArrayList<>(noOfItem);

            int counter = 0;
            List<String> ids = new ArrayList<>(noOfItem);
            List<MBQMessage> messagesPulled = new ArrayList<>(noOfItem);
            for (int i = 0; i < noOfItem && counter < seqQ.size(); ) {
                String id = seqQ.get(counter);
                MBQMessage item = getQueue().get(queueName, id);
                List<MBQMessage> allMessages = getQueue().get(queueName, item.getSeqKey(), Arrays.asList(QueueStatus.PROCESSING, QueueStatus.ERROR, QueueStatus.HELD));
                if (allMessages.isEmpty()) {
                    ids.add(id);
                    messagesPulled.add(item);
                    items.add(item);
                    i++;
                }
                counter++;
            }
            messagesPulled.forEach(item -> item.updateStatus(QueueStatus.PROCESSING));
            seqQ.removeAll(ids);

            //Add Client messages ID that is in Process
            CLIENTS_MESSAGES.put(client, ids);

            return items;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public boolean commit(Client client, List<String> ids) {
        return updateQueueStatus(client, ids, QueueStatus.COMPLETED);
    }

    @Override
    public boolean rollback(Client client, List<String> ids) {
        return updateQueueStatus(client, ids, QueueStatus.PENDING);
    }

    private boolean updateQueueStatus(Client client, List<String> ids, QueueStatus status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        if (status == QueueStatus.PROCESSING) {
            throw new MBQException("Can't mark item to Processing");
        }

        validateClient(client);

        //PROCESSING-> COMPLETE
        List<String> processingToComplete = new ArrayList<>();

        //ERROR/HELD/BLOCKED-> COMPLETE
        List<String> restAllToComplete = new ArrayList<>();

        //ANY-> HELD/BLOCKED/ERROR
        List<String> allToNotCompleted = new ArrayList<>();

        ids.forEach(id -> {
            MBQMessage existingMsg = getQueue().get(client.getQueueName(), id);
            if (existingMsg == null) {
                throw new MBQException("Message didn't find in the queue with id " + id);
            }

            if (status == QueueStatus.COMPLETED) {
                if (existingMsg.getStatus() == QueueStatus.PROCESSING) {
                    processingToComplete.add(id);
                } else if (existingMsg.getStatus() != QueueStatus.COMPLETED) {
                    restAllToComplete.add(id);
                }
            } else {
                allToNotCompleted.add(id);
            }
        });

        if (!processingToComplete.isEmpty()) {
            getQueue().updateStatus(client.getQueueName(), processingToComplete, status);
        }

        if (!restAllToComplete.isEmpty()) {
            getQueue().updateStatus(client.getQueueName(), restAllToComplete, status);
            getPendingQueueMap().get(client.getQueueName()).removeAll(restAllToComplete);
        }

        if (!allToNotCompleted.isEmpty()) {
            getQueue().updateStatus(client.getQueueName(), restAllToComplete, status);
            pushIdToRightPlace(getPendingQueueMap().get(client.getQueueName()), allToNotCompleted);
        }

        //Remove Client Messages once processing is done.
        CLIENTS_MESSAGES.remove(client);
        return true;
    }

    private void pushIdToRightPlace(PendingQueue<String> seqQ, List<String> idsToPushed) {
        Collections.sort(idsToPushed);

        if (seqQ.isEmpty()) {
            seqQ.addAll(idsToPushed);
        } else {
            idsToPushed.forEach(id -> {
                int index = -1;
                for (int i = 0; i < seqQ.size(); i++) {
                    if (seqQ.get(i).compareTo(id) <= 0) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    seqQ.addLast(id);
                } else {
                    seqQ.add(index, id);
                }
            });
        }
    }

    @Override
    public List<MBQMessage> push(Client client, List<QMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new MBQException("Message to be pushed can't be blank");
        }
        validateClient(client);
        List<MBQMessage> pushedMsgs = getQueue().push(client.getQueueName(), messages);

        try {
            LOCK.lock();
            pushedMsgs.forEach(msg -> getPendingQueueMap().get(client.getQueueName()).add(msg.getId()));
            System.out.println("No of items in the queue + " + getPendingQueueMap().get(client.getQueueName()).size());
            LOGGER.info("No of items in the queue {}", getPendingQueueMap().get(client.getQueueName()).size());
        } finally {
            LOCK.unlock();
        }

        return pushedMsgs;
    }

    @Override
    public MBQMessage push(Client client, QMessage message) {
        return push(client, Collections.singletonList(message)).get(0);
    }

    @Override
    public String ping(Client client) {
        validateClient(client);
        //TODO: validate Heartbeat of client
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        long currTime = System.currentTimeMillis();
        LOGGER.info("Received heart beat from client [{}] with Id : {} at {}", client, id, currTime);
        CLIENTS_HB.put(id, currTime);

        return HashingUtil.hashSHA256(id + currTime);
    }

    /**
     * Queue implementation for Sub classes
     *
     * @return MB Queue
     */
    protected MBQueue getQueue() {
        return queue;
    }

    /**
     * Pending queue implementation
     *
     * @return Pending Queue Map
     */
    protected PendingQueueMap getPendingQueueMap() {
        return pendingQueueMap;
    }
}
