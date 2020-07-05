package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.MBQDataStore;
import com.rags.tools.mbq.queue.pending.PendingQ;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.collector.MBQStatsService;
import com.rags.tools.mbq.util.HashingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractMBQueueServer implements MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMBQueueServer.class);

    private static final Map<Client, ClientInfo> CLIENTS_HB = new ConcurrentHashMap<>(2048, .9f, 512);
    private static final Map<String, Set<String>> USED_SEQ = new ConcurrentHashMap<>(256, .75f, 64);

    private static final int PING_INTERVAL = 5000;

    private final PendingQMap<IdSeqKey> pendingQMap;

    private final MBQDataStore queueDataStore;

    private final MBQStatsService statsService;

    public AbstractMBQueueServer(MBQDataStore MBQDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsService) {
        this.queueDataStore = MBQDataStore;
        this.pendingQMap = pendingQMap;
        this.statsService = statsService;
        init();
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                List<Client> allInvalids = new ArrayList<>();

                CLIENTS_HB.forEach((client, hb) -> {
                    boolean isInValid = (curTime - hb.getTs()) > PING_INTERVAL;
                    if (isInValid) {
                        LOGGER.warn("Client with ID {} was not active {}, last heart beat received at {}", client.getId(), curTime, hb);
                        allInvalids.add(client);
                    }
                });

                allInvalids.forEach(client -> {
                    rollback(client);
                    CLIENTS_HB.remove(client);
                    statsService.collectConnectedClients(client);
                });
            }
        }, 0, 1000);
    }

    public boolean rollback(Client client) {
        ClientInfo clientInfo = getClientInfo(client);
        //Move Items to the right place if client is not active
        if (clientInfo != null) {
            synchronized (clientInfo) {
                List<IdSeqKey> messages = clientInfo.getMessages();
                if (!messages.isEmpty()) {
                    String queueName = client.getQueueName();
                    PendingQ<IdSeqKey> pendQ = getPendingQueue(queueName);
                    long curr = 0;
                    if (LOGGER.isDebugEnabled()) {
                        curr = System.currentTimeMillis();
                    }
                    messages.forEach(i -> i.setStatus(QueueStatus.PENDING));
                    Collections.reverse(messages);

                    synchronized (pendQ) {
                        pendQ.addAllFirst(messages);
                        USED_SEQ.get(queueName).removeAll(messages.stream().map(IdSeqKey::getSeqKey).collect(Collectors.toList()));
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Total time queue {} was blocked to rollback {} no of messages, is {}", queueName, messages, (System.currentTimeMillis() - curr));
                    }
                    statsService.collectClientRollbackStats(client, messages);
                }
            }
        }
        return true;
    }

    void init() {
        getQueueDataStore().updateStatus(QueueStatus.PROCESSING, QueueStatus.PENDING);
        getQueueDataStore().getAllPendingIds().forEach((queueName, val) -> {
            USED_SEQ.putIfAbsent(queueName, new HashSet<>(20000));
            USED_SEQ.get(queueName).addAll(val.stream().map(IdSeqKey::getSeqKey).collect(Collectors.toSet()));
            getPendingQueue(queueName).addAll(val);
        });
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
        statsService.collectConnectedClients(clientWithId);
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

        if ((curTime - getClientInfo(client).getTs()) > PING_INTERVAL) {
            ClientInfo hb = CLIENTS_HB.remove(client);
            statsService.collectDisconnectedClients(client);
            LOGGER.error("Client [{}] was not active {}", client, hb.getTs());
            throw new MBQException("Client was existing and was not active");
        }
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        validateClient(client);

        String queueName = client.getQueueName();
        PendingQ<IdSeqKey> seqQ = getPendingQueue(queueName);

        if (seqQ.isEmpty()) {
            return Collections.emptyList();
        }

        int batch = client.getBatch(), counter = 0;
        List<IdSeqKey> idSeqKeys = new ArrayList<>();

        int noOfItem = Math.min(seqQ.size(), batch);
        long currTime = System.currentTimeMillis();
        Set<String> markedBlockedSeq = new HashSet<>();
        synchronized (seqQ) {
            for (int i = 0; i < noOfItem && counter < seqQ.size(); counter++) {
                IdSeqKey idKey = seqQ.get(counter);

                if (idKey.getStatus().isBlocking()) {
                    markedBlockedSeq.add(idKey.getSeqKey());
                }

                if (currTime >= idKey.getScheduledAt()
                        && (idKey.getSeqKey() == null || !USED_SEQ.get(queueName).contains(idKey.getSeqKey()))
                        && !markedBlockedSeq.contains(idKey.getSeqKey())
                        && idKey.getStatus() == QueueStatus.PENDING) {
                    idSeqKeys.add(idKey);
                    i++;
                }
            }
            USED_SEQ.get(queueName).addAll(idSeqKeys.stream().map(IdSeqKey::getSeqKey).collect(Collectors.toList()));
            seqQ.removeAll(idSeqKeys);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Total time queue {} was blocked to pull {} no of messages, is {}", queueName, idSeqKeys.size(), (System.currentTimeMillis() - currTime));
        }

        List<MBQMessage> items = getQueueDataStore().get(queueName, idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList()));
        idSeqKeys.forEach(i -> i.setStatus(QueueStatus.PROCESSING));

        items.parallelStream().forEach(i -> i.updateStatus(QueueStatus.PROCESSING));

        ClientInfo clientInfo = getClientInfo(client);
        synchronized (clientInfo) {
            clientInfo.getMessages().addAll(idSeqKeys);
        }

        statsService.collectClientProcessingStats(client, idSeqKeys);
        return items;
    }

    @Override
    public boolean commit(Client client, Map<QueueStatus, List<String>> ids, Map<String, List<QMessage>> messagesToBePushed) {
        //TODO: Wrap around a transaction
        ids.forEach((status, procIds) -> updateQueueStatus(client, procIds, status == QueueStatus.PROCESSING ? QueueStatus.COMPLETED : status));
        if (!ids.isEmpty()) {
//            commit(client, ids);
        }
        if (!messagesToBePushed.isEmpty()) {
            messagesToBePushed.forEach((queueName, messages) -> pushMessage(client, messages, queueName));
        }
        return true;
    }

    @Override
    public boolean rollback(Client client, Map<QueueStatus, List<String>> ids) {
        return rollback(client);
    }

    //New Commit API dedicated to only client processing.
    private void commit(Client client, Map<QueueStatus, List<String>> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new MBQException("Commit can't be called without any message");
        }

        if (ids.containsKey(QueueStatus.PROCESSING) || ids.containsKey(QueueStatus.PENDING) || ids.containsKey(QueueStatus.BLOCKED)) {
            throw new MBQException("Commit can't mark item to PROCESSING/PENDING/BLOCKED");
        }

        List<String> allIds = ids.values().stream().reduce((a, b) -> {
            a.addAll(b);
            return a;
        }).get();

        validateClient(client);

        ClientInfo clientInfo = getClientInfo(client);
        synchronized (clientInfo) {
            List<IdSeqKey> clientMessages = clientInfo.getMessages();
            List<String> clientProcessingIds = clientMessages.stream().map(IdSeqKey::getId).collect(Collectors.toList());
            if (!clientProcessingIds.containsAll(allIds) || !allIds.containsAll(clientProcessingIds)) {
                LOGGER.error("Client Messages : {}, Committed Messages : {}", clientProcessingIds, allIds);
                throw new MBQException("There's a mismatch between messages pulled and commit messages");
            }

            List<IdSeqKey> toBePushedAgain = new LinkedList<>();
            //COMPLETED/ERROR/HELD/PENDING
            ids.forEach((status, msgIds) -> {
                getQueueDataStore().updateStatus(client.getQueueName(), msgIds, status);
                List<IdSeqKey> messages = clientMessages.stream().filter(i -> msgIds.contains(i.getId())).collect(Collectors.toList());
                if (status == QueueStatus.COMPLETED) {
                    //This has Issues
                    statsService.collectClientCompletedStats(client, messages);
                } else {
                    toBePushedAgain.addAll(messages);
                    statsService.collectClientRollbackStats(client, messages);
                }
            });

            PendingQ<IdSeqKey> pendQ = getPendingQueue(client.getQueueName());
            synchronized (pendQ) {
                pendQ.addInOrder(toBePushedAgain);
                USED_SEQ.get(client.getQueueName()).removeAll(clientMessages.stream().map(IdSeqKey::getSeqKey).collect(Collectors.toList()));
            }
            clientMessages.clear();
        }
    }

    private void updateItemStatus(Client client, List<String> ids, QueueStatus status) {
        if (ids == null || ids.isEmpty()) {
            throw new MBQException("Can't update status for blank message");
        }

        if (status == QueueStatus.PROCESSING) {
            throw new MBQException("Can't mark item to Processing");
        }

        validateClient(client);

        List<MBQMessage> messages = getQueueDataStore().get(client.getQueueName(), ids);


    }


    private boolean updateQueueStatus(Client client, List<String> ids, QueueStatus status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        if (status == QueueStatus.PROCESSING) {
            throw new MBQException("Can't mark item to Processing");
        }

        validateClient(client);

        //ERROR/HELD/BLOCKED-> COMPLETE
        List<IdSeqKey> allToComplete = new ArrayList<>();

        //ANY-> HELD/BLOCKED/ERROR
        List<IdSeqKey> allToNotCompleted = new ArrayList<>();

        List<MBQMessage> existingMsgs = getQueueDataStore().get(client.getQueueName(), ids);

        ids.forEach(id -> {
            Optional<MBQMessage> optionalMBQMessage = existingMsgs.stream().filter(m -> m.getId().equals(id)).findFirst();
            if (optionalMBQMessage.isEmpty()) {
                throw new MBQException("Message didn't find in the queue with id " + id);
            }

            MBQMessage message = optionalMBQMessage.get();
            if (status == QueueStatus.COMPLETED) {
                allToComplete.add(new IdSeqKey(message.getId(), message.getSeqKey(), message.getStatus(), message.getScheduledAt()));
            } else {
                allToNotCompleted.add(new IdSeqKey(message.getId(), message.getSeqKey(), status, message.getScheduledAt()));
            }
        });


        List<IdSeqKey> allItems = new LinkedList<>(allToComplete);
        allItems.addAll(allToNotCompleted);

        getQueueDataStore().updateStatus(client.getQueueName(), allItems.stream().map(IdSeqKey::getId).collect(Collectors.toList()), status);

        PendingQ<IdSeqKey> pendQ = getPendingQueue(client.getQueueName());
        synchronized (pendQ) {
            if (!allToComplete.isEmpty()) {
                pendQ.removeAll(allToComplete);
                USED_SEQ.get(client.getQueueName()).removeAll(allToComplete.stream().map(IdSeqKey::getSeqKey).collect(Collectors.toList()));
            }

            if (!allToNotCompleted.isEmpty()) {
                //TODO: Handle case when Item is in the queue and has to be marked HELD/BLOCKED/ERROR..Do not add to the first.
                pendQ.addAllFirst(allToNotCompleted);
            }
        }
        //Remove Client Messages once processing is done. //TODO: Validate against messages in Server
        statsService.collectClientCompletedStats(client, allItems);
        ClientInfo clientInfo = getClientInfo(client);
        synchronized (clientInfo) {
            clientInfo.getMessages().clear();
        }
        return true;
    }

    private ClientInfo getClientInfo(Client client) {
        return CLIENTS_HB.get(client);
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
        List<MBQMessage> pushedMsgs = getQueueDataStore().push(queueName, messages);
        List<IdSeqKey> ids = pushedMsgs.stream().map(i -> new IdSeqKey(i.getId(), i.getSeqKey(), i.getStatus(), i.getScheduledAt())).collect(Collectors.toList());

        synchronized (pendQ) {
            pendQ.addAll(ids);
            LOGGER.debug("Total No of items in the queue {} is {}", queueName, pendQ.size());
            USED_SEQ.putIfAbsent(client.getQueueName(), new HashSet<>(20000));
        }

        statsService.collectPendingStats(queueName, messages.size());
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
        ClientInfo clientInfo = new ClientInfo(currTime, hb);
        if (CLIENTS_HB.containsKey(client)) {
            ClientInfo info = getClientInfo(client);
            synchronized (info) {
                clientInfo.getMessages().addAll(info.getMessages());
            }
        }
        CLIENTS_HB.put(client, clientInfo);

        return hb;
    }

    protected MBQDataStore getQueueDataStore() {
        return queueDataStore;
    }

    protected PendingQ<IdSeqKey> getPendingQueue(String queueName) {
        return pendingQMap.get(queueName);
    }

    private static class ClientInfo {
        private long ts;
        private String hb;
        private List<IdSeqKey> messages = new ArrayList<>();

        public ClientInfo(long ts, String hb) {
            this.ts = ts;
            this.hb = hb;
        }

        public long getTs() {
            return ts;
        }

        public String getHb() {
            return hb;
        }

        public List<IdSeqKey> getMessages() {
            return messages;
        }
    }
}
