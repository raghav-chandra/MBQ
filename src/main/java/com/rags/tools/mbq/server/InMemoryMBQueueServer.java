package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.InMemoryMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.util.HashingUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMBQueueServer implements MBQueueServer {

    private static final MBQueue QUEUE = new InMemoryMBQueue();
    private static final Map<String, LinkedList<String>> ALL_PENDING_MESSAGES = new ConcurrentHashMap<>();

    private static final Map<String, Long> CLIENTS_HB = new ConcurrentHashMap<>();

    private static final int PING_INTERVAL = 5000;

    public InMemoryMBQueueServer() {
        /*new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                List<String> allInvalids = new ArrayList<>();

                CLIENTS_HB.forEach((id, val) -> {
                    boolean isInValid = (curTime - val) > PING_INTERVAL;
                    if (isInValid) {
                        allInvalids.add(id);
                    }
                });

                allInvalids.forEach(CLIENTS_HB::remove);
            }
        }, 0, 1000);*/
    }

    @Override
    public Client registerClient(QConfig config) {
        String id = getClientID(config.getWorkerName(), config.getPollingQueue(), config.getBatch());

        if (CLIENTS_HB.containsKey(id)) {
            throw new MBQException("Client is already registered");
        }

        Client client = new Client(id, config.getWorkerName(), config.getPollingQueue(), "localhost", config.getBatch());
        CLIENTS_HB.put(id, System.currentTimeMillis());
        return client;
    }

    private String getClientID(String workerName, String pollingQueue, int batch) {
        return HashingUtil.hashSHA256(workerName + pollingQueue + batch);
    }

    private void validateClient(Client client) {
        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());
        if (!CLIENTS_HB.containsKey(id)) {
            throw new MBQException("Client is not registered or wasn't active");
        }

        long curTime = System.currentTimeMillis();

        /*if ((curTime - CLIENTS_HB.get(id)) > PING_INTERVAL) {
            CLIENTS_HB.remove(id);
            throw new MBQException("Client was existing and was not active");
        }*/
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        validateClient(client);

        String queueName = client.getQueueName();
        int batch = client.getBatch();

        if (ALL_PENDING_MESSAGES.containsKey(queueName)) {
            LinkedList<String> seqQ = ALL_PENDING_MESSAGES.get(queueName);
            if (seqQ.isEmpty()) {
                return Collections.emptyList();
            }

            int noOfItem = Math.min(seqQ.size(), batch);
            List<MBQMessage> items = new ArrayList<>(noOfItem);

            int counter = 0;
            for (int i = 0; i < noOfItem && counter < seqQ.size(); ) {
                String id = seqQ.get(counter++);
                MBQMessage item = QUEUE.get(queueName, id);
                List<MBQMessage> allMessages = QUEUE.get(queueName, item.getSeqKey(), Arrays.asList(QueueStatus.PROCESSING, QueueStatus.ERROR, QueueStatus.HELD));
                if (allMessages.isEmpty()) {
                    seqQ.remove(counter);
                    item.updateStatus(QueueStatus.PROCESSING);
                    items.add(item);
                    i++;
                }
            }
            return items;
        }

        return Collections.emptyList();
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
            MBQMessage existingMsg = QUEUE.get(client.getQueueName(), id);
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
            QUEUE.updateStatus(client.getQueueName(), processingToComplete, status);
        }

        if (!restAllToComplete.isEmpty()) {
            QUEUE.updateStatus(client.getQueueName(), restAllToComplete, status);
            LinkedList<String> seqQ = ALL_PENDING_MESSAGES.get(client.getQueueName());
            if (seqQ != null) {
                seqQ.removeAll(restAllToComplete);
            }
        }

        if (!allToNotCompleted.isEmpty()) {
            QUEUE.updateStatus(client.getQueueName(), restAllToComplete, status);
            if (!ALL_PENDING_MESSAGES.containsKey(client.getQueueName())) {
                ALL_PENDING_MESSAGES.put(client.getQueueName(), new LinkedList<>());
            }
            pushIdToRightPlace(ALL_PENDING_MESSAGES.get(client.getQueueName()), allToNotCompleted);
        }

        return true;
    }

    private void pushIdToRightPlace(LinkedList<String> seqQ, List<String> idsToPushed) {
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

        List<MBQMessage> pushedMsgs = QUEUE.push(client.getQueueName(), messages);

        if (!ALL_PENDING_MESSAGES.containsKey(client.getQueueName())) {
            ALL_PENDING_MESSAGES.put(client.getQueueName(), new LinkedList<>());
        }

        pushedMsgs.forEach(msg -> ALL_PENDING_MESSAGES.get(client.getQueueName()).add(msg.getId()));

        return pushedMsgs;
    }

    @Override
    public MBQMessage push(Client client, QMessage message) {
        return push(client, Collections.singletonList(message)).get(0);
    }

    @Override
    public void ping(Client client) {
        validateClient(client);

        String id = getClientID(client.getName(), client.getQueueName(), client.getBatch());

        CLIENTS_HB.put(id, System.currentTimeMillis());
    }
}
