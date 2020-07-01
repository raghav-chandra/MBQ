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
    private Map<String, QueueStats> qStats = new ConcurrentHashMap<>();

    private Map<String, ClientStats> clientStats = new ConcurrentHashMap<>();

    public Collection<QueueStats> getQStats() {
        return qStats.values();
    }

    public Collection<String> getAllQueueNames() {
        return qStats.keySet();
    }

    public int getNoOfItemsInTheQueue(String queueName) {
        if (qStats.containsKey(queueName)) {
            return qStats.get(queueName).getDepth();
        }
        return 0;
    }

    public void addClient(Client client) {
        clientStats.put(client.getId(), new ClientStats(client.getId()));
    }

    public void removeClient(Client client) {
        clientStats.remove(client.getId());
    }


    public void addQueueStats(String queueName, QueueStatus status, int noOfItems) {
        qStats.putIfAbsent(queueName, new QueueStats(queueName));
        qStats.get(queueName).addProcessed(status, noOfItems);
    }

    public void addQueueStats(String queueName, Map<QueueStatus, Integer> processed) {
        qStats.putIfAbsent(queueName, new QueueStats(queueName));
        qStats.get(queueName).addProcessed(processed);
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

    public void reset() {
        qStats.clear();
        clientStats.clear();
    }

    public void addClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.PROCESSING, processingIds);
        qStats.get(client.getQueueName()).addProcessing(processingIds);
    }

    public void addClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.COMPLETED, processingIds);
        qStats.get(client.getQueueName()).markCompleted(processingIds);
    }
    public void addClientErrorStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.ERROR, processingIds);
        qStats.get(client.getQueueName()).markError(processingIds);
    }
    /*public void addClientHeldStats(Client client, List<IdSeqKey> idSeqKeys) {
        List<String> processingIds = idSeqKeys.stream().map(IdSeqKey::getId).collect(Collectors.toList());
        clientStats.get(client.getId()).addProcessed(QueueStatus.HELD, processingIds);
        qStats.get(client.getQueueName()).markError(processingIds);
    }*/
}
