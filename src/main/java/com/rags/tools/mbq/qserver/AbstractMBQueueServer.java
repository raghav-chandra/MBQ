package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.PendingQ;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.util.HashingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractMBQueueServer implements MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMBQueueServer.class);

    private static final Map<Client, ClientInfo> CLIENTS_HB = new ConcurrentHashMap<>();

    private static final int PING_INTERVAL = 5000;

    private final PendingQMap<IdSeqKey> pendingQMap;

    private final MBQueue queue;

    public AbstractMBQueueServer(MBQueue mbQueue, PendingQMap<IdSeqKey> pendingQMap) {
        this.queue = mbQueue;
        this.pendingQMap = pendingQMap;
        init();
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                List<Client> allInvalids = new ArrayList<>();

                CLIENTS_HB.forEach((client, hb) -> {
                    boolean isInValid = (curTime - hb.getTimeStamp()) > PING_INTERVAL;
                    if (isInValid) {
                        LOGGER.warn("Client with ID {} was not active {}, last heart beat received at {}", client.getId(), curTime, hb);
                        allInvalids.add(client);
                    }
                });

                allInvalids.forEach(client -> {
                    ClientInfo clientInfo = CLIENTS_HB.get(client);
                    //Move Items to the right place if client is not active
                    if (clientInfo != null && !clientInfo.getMessages().isEmpty()) {
                        PendingQ<IdSeqKey> pendQ = getPendingQueue(client.getQueueName());
                        synchronized (pendQ) {
                            pendQ.addAllFirst(clientInfo.getMessages());
                        }
                    }
                    CLIENTS_HB.remove(client);

                });
            }
        }, 0, 1000);
    }

    void init() {
        getQueue().updateStatus(QueueStatus.PROCESSING, QueueStatus.PENDING);
        getQueue().getAllPendingIds().forEach((key, val) -> getPendingQueue(key).addAll(val));
    }

    @Override
    public Client registerClient(Client client) {
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());

        if (CLIENTS_HB.containsKey(client)) {
            LOGGER.error("Client already registered with name {}, queue {} and batch {}", client.getName(), client.getQueueName(), client.getBatch());
            throw new MBQException("Client is already registered");
        }

        Client clientWithId = new Client(id, client.getName(), client.getQueueName(), client.getBatch());
        long currTime = System.currentTimeMillis();

        clientWithId.setHeartBeatId(HashingUtil.hashSHA256(id + currTime));
        CLIENTS_HB.put(clientWithId, new ClientInfo(currTime, clientWithId.getHeartBeatId()));

        return clientWithId;
    }

    private String getClientID(String workerName, String pollingQueue, int batch) {
        return HashingUtil.hashSHA256(workerName + pollingQueue + batch);
    }

    private void validateClient(Client client) {
        if (!CLIENTS_HB.containsKey(client)/* || !client.getHeartBeatId().equals(CLIENTS_HB.get(id).getHeartBeat())*/) {
            LOGGER.error("Client [{}] wasn't active or dint exists", client);
            throw new MBQException("Client is not registered or wasn't active");
        }

        long curTime = System.currentTimeMillis();

        if ((curTime - CLIENTS_HB.get(client).getTimeStamp()) > PING_INTERVAL) {
            ClientInfo hb = CLIENTS_HB.remove(client);
            LOGGER.error("Client [{}] was not active {}", client, hb.getTimeStamp());
            throw new MBQException("Client was existing and was not active");
        }
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        validateClient(client);

        String queueName = client.getQueueName();
        int batch = client.getBatch();

        PendingQ<IdSeqKey> seqQ = getPendingQueue(queueName);

        List<MBQMessage> items = new ArrayList<>();
        int counter = 0;
        List<String> ids = new ArrayList<>();
        if (seqQ.isEmpty()) {
            return Collections.emptyList();
        }

        int noOfItem = Math.min(seqQ.size(), batch);
        long currTime = System.currentTimeMillis();
        synchronized (seqQ) {
            for (int i = 0; i < noOfItem && counter < seqQ.size(); ) {
                IdSeqKey idKey = seqQ.get(counter);
                //TODO: Query ony once. Calculate all Ids and then release lock on the queue
                MBQMessage item = getQueue().get(queueName, idKey.getId());

                if (currTime > item.getScheduledAt()) {
                    if (QMessage.DEFAULT_SEQ.equals(idKey.getSeqKey())) {
                        ids.add(idKey.getId());
                        items.add(item);
                        i++;
                    } else {
                        boolean seqKeyUsed = false;
                        for (int j = 0; j < counter; j++) {
                            if (seqQ.get(j).getSeqKey().equals(idKey.getSeqKey()) && !ids.contains(seqQ.get(j).getId())) {
                                seqKeyUsed = true;
                            }
                        }
                        if (!seqKeyUsed) {
                            ids.add(idKey.getId());
                            items.add(item);
                            i++;
                        }
                    }
                }
                counter++;
            }
            seqQ.removeAll(items.stream().map(i -> new IdSeqKey(i.getId(), i.getSeqKey())).collect(Collectors.toList()));
        }

        items.parallelStream().forEach(i -> i.updateStatus(QueueStatus.PROCESSING));

        CLIENTS_HB.get(client).getMessages().addAll(items.stream().map(item -> new IdSeqKey(item.getId(), item.getSeqKey())).collect(Collectors.toList()));
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
        List<IdSeqKey> restAllToComplete = new ArrayList<>();

        //ANY-> HELD/BLOCKED/ERROR
        List<IdSeqKey> allToNotCompleted = new ArrayList<>();

        List<MBQMessage> existingMsgs = getQueue().get(client.getQueueName(), ids);

        ids.forEach(id -> {
            Optional<MBQMessage> optionalMBQMessage = existingMsgs.stream().filter(m -> m.getId().equals(id)).findFirst();
            if (optionalMBQMessage.isEmpty()) {
                throw new MBQException("Message didn't find in the queue with id " + id);
            }

            MBQMessage message = optionalMBQMessage.get();
            if (status == QueueStatus.COMPLETED) {
                if (message.getStatus() == QueueStatus.PROCESSING) {
                    processingToComplete.add(id);
                } else if (message.getStatus() != QueueStatus.COMPLETED) {
                    restAllToComplete.add(new IdSeqKey(message.getId(), message.getSeqKey()));
                }
            } else {
                allToNotCompleted.add(new IdSeqKey(message.getId(), message.getSeqKey()));
            }
        });


        List<String> allItems = new LinkedList<>(processingToComplete);
        allItems.addAll(restAllToComplete.parallelStream().map(IdSeqKey::getId).collect(Collectors.toList()));
        allItems.addAll(allToNotCompleted.parallelStream().map(IdSeqKey::getId).collect(Collectors.toList()));

        getQueue().updateStatus(client.getQueueName(), allItems, status);

        PendingQ<IdSeqKey> pendQ = getPendingQueue(client.getQueueName());
        synchronized (pendQ) {
            if (!restAllToComplete.isEmpty()) {
                pendQ.removeAll(restAllToComplete);
            }

            if (!allToNotCompleted.isEmpty()) {
                pendQ.addAllFirst(allToNotCompleted);
            }
        }
        //Remove Client Messages once processing is done.
        CLIENTS_HB.get(client).getMessages().clear();
        return true;
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

        PendingQ<IdSeqKey> pendQ = getPendingQueue(queueName);
        List<MBQMessage> pushedMsgs = getQueue().push(queueName, messages);
        List<IdSeqKey> ids = pushedMsgs.stream().map(i -> new IdSeqKey(i.getId(), i.getSeqKey())).collect(Collectors.toList());

        synchronized (pendQ) {
            pendQ.addAll(ids);
            LOGGER.debug("Total No of items in the queue {} is {}", queueName, pendQ.size());
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
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        long currTime = System.currentTimeMillis();
        LOGGER.debug("Received heart beat from client [{}] with Id : {} at {}", client, id, currTime);

        String hb = HashingUtil.hashSHA256(id + currTime);
        CLIENTS_HB.put(client, new ClientInfo(currTime, hb));

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

    protected PendingQ<IdSeqKey> getPendingQueue(String queueName) {
        return pendingQMap.get(queueName);
    }

    private static class ClientInfo {
        private long timeStamp;
        private String heartBeat;
        private List<IdSeqKey> messages = new ArrayList<>();

        public ClientInfo(long timeStamp, String hb) {
            this.timeStamp = timeStamp;
            this.heartBeat = hb;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getHeartBeat() {
            return heartBeat;
        }

        public List<IdSeqKey> getMessages() {
            return messages;
        }
    }
}
