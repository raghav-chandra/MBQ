package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MBQStats {
    private Map<String, QueueStats> queueStats = new ConcurrentHashMap<>();

    private Map<String, ClientStats> clientStats = new ConcurrentHashMap<>();

    public Collection<QueueStats> getQStats() {
        return queueStats.values();
    }

    public Collection<String> getAllQueueNames() {
        return queueStats.keySet();
    }

    public int getNoOfItemsInTheQueue(String queueName) {
        if (queueStats.containsKey(queueName)) {
            return queueStats.get(queueName).getDepth();
        }
        return 0;
    }

    public Collection<ClientStats> getClientStats() {
        return clientStats.values();
    }

    public int getNoOfClientsConnected() {
        return clientStats.entrySet().size();
    }

    public int getConnectedClientIds() {
        return clientStats.entrySet().size();
    }

    public Map<String, QueueStats> getQueueStats() {
        return queueStats;
    }

    public void reset() {
        queueStats.clear();
        clientStats.clear();
    }

    public void addClient(Client client) {
        clientStats.put(client.getId(), new ClientStats(client.getId()));
    }

    public void removeClient(Client client) {
        clientStats.remove(client.getId());
    }


    public synchronized void addPendingItemStats(String queueName, int noOfItems) {
        queueStats.putIfAbsent(queueName, new QueueStats(queueName));
        queueStats.get(queueName).addPending(noOfItems);
    }

    public synchronized void addClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.PROCESSING, processingIds);
        queueStats.get(client.getQueueName()).addProcessing(processingIds);
    }

    public synchronized void addClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.COMPLETED, processingIds);
        queueStats.get(client.getQueueName()).markCompleted(processingIds);
    }

    public synchronized void addClientErrorStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.ERROR, processingIds);
        queueStats.get(client.getQueueName()).markError(processingIds);
    }

    public synchronized void addClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).removeProcessing(processingIds);
        queueStats.get(client.getQueueName()).markRolledBack(processingIds);
    }
}
