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

    private static final Map<String, HeartBeat> CLIENTS_HB = new ConcurrentHashMap<>();
    private static final Map<Client, List<String>> CLIENTS_MESSAGES = new ConcurrentHashMap<>();

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final int PING_INTERVAL = 5000;

    private final PendingQueueMap pendingQueueMap;

    private final MBQueue queue;

    public AbstractMBQueueServer(MBQueue mbQueue, PendingQueueMap pendingQueueMap) {
        this.queue = mbQueue;
        this.pendingQueueMap = pendingQueueMap;
        init();
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                List<String> allInvalids = new ArrayList<>();

                CLIENTS_HB.forEach((id, hb) -> {
                    boolean isInValid = (curTime - hb.getTimeStamp()) > PING_INTERVAL;
                    if (isInValid) {
                        LOGGER.warn("Client with ID {} was not active {}, last heart beat received at {}", id, curTime, hb);
                        allInvalids.add(id);
                    }
                });

                allInvalids.forEach(id -> {
                    CLIENTS_HB.remove(id);
                    //Move Items to the right place if client is not active
                    Optional<Client> existing = CLIENTS_MESSAGES.keySet().parallelStream().filter(c -> c.getId().equals(id)).findFirst();
                    if (existing.isPresent()) {
                        Client client = existing.get();
                        PendingQueue<String> pendQ = getPendingQueue(client.getQueueName());
                        pendQ.lock();
                        try {
                            pushIdToRightPlace(pendQ, CLIENTS_MESSAGES.get(client));
                        } finally {
                            pendQ.unlock();
                        }
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

        clientWithId.setHeartBeatId(HashingUtil.hashSHA256(id + currTime));
        CLIENTS_HB.put(id, new HeartBeat(currTime, clientWithId.getHeartBeatId()));

        return clientWithId;
    }

    private String getClientID(String workerName, String pollingQueue, int batch) {
        return HashingUtil.hashSHA256(workerName + pollingQueue + batch);
    }

    private void validateClient(Client client) {
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        if (!CLIENTS_HB.containsKey(id)/* || !client.getHeartBeatId().equals(CLIENTS_HB.get(id).getHeartBeat())*/) {
            LOGGER.error("Client [{}] wasn't active or dint exists", client);
            throw new MBQException("Client is not registered or wasn't active");
        }

        long curTime = System.currentTimeMillis();

        if ((curTime - CLIENTS_HB.get(id).getTimeStamp()) > PING_INTERVAL) {
            HeartBeat hb = CLIENTS_HB.remove(id);
            LOGGER.error("Client [{}] was not active {}", client, hb.getTimeStamp());
            throw new MBQException("Client was existing and was not active");
        }
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        validateClient(client);

        String queueName = client.getQueueName();
        int batch = client.getBatch();

        PendingQueue<String> seqQ = getPendingQueue(queueName);
        if (seqQ.isEmpty()) {
            return Collections.emptyList();
        }

        int noOfItem = Math.min(seqQ.size(), batch);
        List<MBQMessage> items = new ArrayList<>(noOfItem);

        int counter = 0;
        List<String> ids = new ArrayList<>(noOfItem);

        try {
//            LOCK.lock();
            seqQ.lock();
//            List<MBQMessage> messagesPulled = new ArrayList<>(noOfItem);
            for (int i = 0; i < noOfItem && counter < seqQ.size(); ) {
                String id = seqQ.get(counter);
                MBQMessage item = getQueue().get(queueName, id);
                List<MBQMessage> allMessages = getQueue().get(queueName, item.getSeqKey(), Arrays.asList(QueueStatus.PROCESSING, QueueStatus.ERROR, QueueStatus.HELD));
                if (allMessages.isEmpty()) {
                    ids.add(id);
//                    messagesPulled.add(item);
                    items.add(item);
                    i++;
                }
                counter++;
            }

            getQueue().updateStatus(queueName, ids, QueueStatus.PROCESSING);
//            messagesPulled.forEach(item ->  getQueue().updateStatus()item.updateStatus(QueueStatus.PROCESSING));
            seqQ.removeAll(ids);
            //Add Client messages ID that is in Process
        } finally {
            seqQ.unlock();
//            LOCK.unlock();
        }

        CLIENTS_MESSAGES.put(client, ids);
        return items;
    }

    @Override
    public boolean commit(Client client, List<String> ids, Map<String, List<QMessage>> messagesToBePushed) {
        //TODO: Wrap around a transaction
        updateQueueStatus(client, ids, QueueStatus.COMPLETED);
        if (!messagesToBePushed.isEmpty()) {
            messagesToBePushed.forEach((queueName, messages) -> pushMessage(client, messages, queueName));
        }
        return true;
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

        List<MBQMessage> existingMsgs = getQueue().get(client.getQueueName(), ids);

        ids.forEach(id -> {
            Optional<MBQMessage> message = existingMsgs.stream().filter(m -> m.getId().equals(id)).findFirst();
            if (message.isEmpty()) {
                throw new MBQException("Message didn't find in the queue with id " + id);
            }

            if (status == QueueStatus.COMPLETED) {
                if (message.get().getStatus() == QueueStatus.PROCESSING) {
                    processingToComplete.add(id);
                } else if (message.get().getStatus() != QueueStatus.COMPLETED) {
                    restAllToComplete.add(id);
                }
            } else {
                allToNotCompleted.add(id);
            }
        });


        List<String> allItems = new LinkedList<>(processingToComplete);
        allItems.addAll(restAllToComplete);
        allItems.addAll(allToNotCompleted);

        getQueue().updateStatus(client.getQueueName(), allItems, status);

        PendingQueue<String> pendQ = getPendingQueue(client.getQueueName());
        try {
            pendQ.lock();
            if (!restAllToComplete.isEmpty()) {
                pendQ.removeAll(restAllToComplete);
            }

            if (!allToNotCompleted.isEmpty()) {
                pendQ.addAll(allToNotCompleted);
                pushIdToRightPlace(pendQ, allToNotCompleted);

            }
        } finally {
            pendQ.unlock();
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
        return pushMessage(client, messages, client.getQueueName());
    }

    private List<MBQMessage> pushMessage(Client client, List<QMessage> messages, String queueName) {
        if (messages == null || messages.isEmpty()) {
            throw new MBQException("Message to be pushed can't be blank");
        }
        validateClient(client);

        PendingQueue<String> pendQ = getPendingQueue(queueName);
        try {
//            LOCK.lock();
            List<MBQMessage> pushedMsgs = getQueue().push(queueName, messages);

            pendQ.lock();
            pushedMsgs.forEach(msg -> getPendingQueue(queueName).add(msg.getId()));
            System.out.println("No of items in the queue + " + getPendingQueue(queueName).size());
            LOGGER.info("No of items in the queue {}", getPendingQueue(queueName).size());
            return pushedMsgs;
        } finally {
            pendQ.unlock();
//            LOCK.unlock();
        }
    }

    @Override
    public MBQMessage push(Client client, QMessage message) {
        return push(client, Collections.singletonList(message)).get(0);
    }

    @Override
    public String ping(Client client) {
        validateClient(client);
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        long currTime = System.currentTimeMillis();
        LOGGER.info("Received heart beat from client [{}] with Id : {} at {}", client, id, currTime);

        String hb = HashingUtil.hashSHA256(id + currTime);
        CLIENTS_HB.put(id, new HeartBeat(currTime, hb));

        return hb;
    }

    /**
     * Queue implementation for Sub classes
     *
     * @return MB Queue
     */
    protected MBQueue getQueue() {
        return queue;
    }

    protected PendingQueue<String> getPendingQueue(String queueName) {
        return pendingQueueMap.get(queueName);
    }

    private static class HeartBeat {
        private long timeStamp;
        private String heartBeat;

        public HeartBeat(long timeStamp, String hb) {
            this.timeStamp = timeStamp;
            this.heartBeat = hb;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getHeartBeat() {
            return heartBeat;
        }
    }
}
