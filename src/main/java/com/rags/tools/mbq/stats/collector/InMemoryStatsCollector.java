package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.MBQStats;

import java.util.List;
import java.util.Map;

public class InMemoryStatsCollector implements MBQStatsCollector {

    private final MBQStats stats = new MBQStats();

    @Override
    public void collectConnectedClients(Client client) {
        this.stats.addClient(client);
    }

    @Override
    public void collectDisconnectedClients(Client client) {
        this.stats.removeClient(client);
    }

    @Override
    public void collectQueueStats(String queueName, QueueStatus status, int noOfItems) {
        this.stats.addQueueStats(queueName, status, noOfItems);
    }

    @Override
    public void collectQueueStats(String queueName, Map<QueueStatus, Integer> processed) {
        this.stats.addQueueStats(queueName, processed);
    }

    @Override
    public void resetStats() {
        this.stats.reset();
    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        stats.addClientProcessingStats(client, idSeqKeys);
    }
}
