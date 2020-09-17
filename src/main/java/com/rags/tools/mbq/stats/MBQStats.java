package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MBQStats {
    private Map<String, QueueStats> queueStats = new ConcurrentHashMap<>();

    private Map<String, ClientStats> clientStats = new ConcurrentHashMap<>();

    public Map<String, ClientStats> getClientStats() {
        return clientStats;
    }

    public Map<String, QueueStats> getQueueStats() {
        return queueStats;
    }

    public void reset() {
        queueStats.clear();
        clientStats.clear();
    }

    public void addClient(Client client) {
        clientStats.put(client.getId(), new ClientStats(client.getId(), client.getName(), client.getHost()));
    }

    public void removeClient(Client client) {
        clientStats.remove(client.getId());
    }

    public synchronized void addPendingItemStats(Client client, String queueName, int noOfItems) {
        queueStats.putIfAbsent(queueName, new QueueStats(queueName));
        queueStats.get(queueName).addPending(noOfItems);
        clientStats.putIfAbsent(client.getId(), new ClientStats(client.getId(), client.getName(), client.getHost()));
        clientStats.get(client.getId()).addPending(noOfItems);
    }

    public synchronized void addClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        putIfAbsent(client);

        clientStats.get(client.getId()).addProcessed(QueueStatus.PROCESSING, processingIds);
        queueStats.get(client.getQueueName()).addProcessing(processingIds);
    }

    private void putIfAbsent(Client client) {
        queueStats.putIfAbsent(client.getQueueName(), new QueueStats(client.getQueueName()));
        clientStats.putIfAbsent(client.getId(), new ClientStats(client.getId(), client.getName(), client.getHost()));
    }

    public synchronized void addClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        putIfAbsent(client);
        clientStats.get(client.getId()).addProcessed(QueueStatus.COMPLETED, processingIds);
        queueStats.get(client.getQueueName()).markCompleted(processingIds);
    }

    public synchronized void addClientErrorStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        putIfAbsent(client);
        clientStats.get(client.getId()).addProcessed(QueueStatus.ERROR, processingIds);
        queueStats.get(client.getQueueName()).markError(processingIds);
    }

    public synchronized void addClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        putIfAbsent(client);
        clientStats.get(client.getId()).removeProcessing(processingIds);
        queueStats.get(client.getQueueName()).markRolledBack(processingIds);
    }

    public synchronized void markOldest(String queueName, IdSeqKey item) {
        queueStats.putIfAbsent(queueName, new QueueStats(queueName));
        queueStats.get(queueName).markOldest(item);
    }

    public synchronized void initStats(String queue, List<IdSeqKey> items) {
        markOldest(queue, items.get(0));

        Client client = new Client("1", "Initialized", queue, 0, "localhost");
        Map<QueueStatus, List<IdSeqKey>> statusMap = items.parallelStream().reduce(new HashMap<>(), (acc, msg) -> {
            acc.putIfAbsent(msg.getStatus(), new LinkedList<>());
            acc.get(msg.getStatus()).add(msg);
            return acc;
        }, (map1, map2) -> map1);

        statusMap.forEach((status, messages) -> {
            switch (status) {
                case ERROR:
                    addClientErrorStats(client, messages);
                    break;
                case PENDING:
                case HELD:
                case BLOCKED:
                    addPendingItemStats(new Client("1", "Initialized", queue, 0, "localhost"), queue, messages.size());
                    break;
                default:
                    throw new MBQException("Don't understand status " + status);
            }
        });
    }
}
