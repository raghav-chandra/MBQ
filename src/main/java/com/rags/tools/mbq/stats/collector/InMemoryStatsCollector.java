package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.MBQStats;

import java.util.List;

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
    public void collectPendingStats(String queueName, int noOfItems) {
        this.stats.addPendingItemStats(queueName, noOfItems);
    }

/*    @Override
    public void collectPendingStats(String queueName, Map<QueueStatus, Integer> processed) {
        this.stats.addPendingItemStats(queueName, processed);
    }*/

    @Override
    public void resetStats() {
        this.stats.reset();
    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        stats.addClientProcessingStats(client, idSeqKeys);
    }

    @Override
    public void collectClientProcessedStats(Client client, List<IdSeqKey> idSeqKeys) {
        stats.addClientCompletedStats(client, idSeqKeys);
    }

    @Override
    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        stats.addClientRollbackStats(client, idSeqKeys);
    }
}