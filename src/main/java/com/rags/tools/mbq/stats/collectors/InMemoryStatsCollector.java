package com.rags.tools.mbq.stats.collectors;

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
    public void collectPendingStats(Client client, String queueName, int noOfItems) {
        this.stats.addPendingItemStats(client, queueName, noOfItems);
    }

    @Override
    public void resetStats() {
        this.stats.reset();
    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        this.stats.addClientProcessingStats(client, idSeqKeys);
    }

    @Override
    public void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        for (IdSeqKey idSeqKey : idSeqKeys) {
            switch (idSeqKey.getStatus()) {
                case PENDING:
                    this.stats.addClientRollbackStats(client, List.of(idSeqKey));
                    break;
                case COMPLETED:
                    this.stats.addClientCompletedStats(client, List.of(idSeqKey));
                    break;
                case ERROR:
                    this.stats.addClientErrorStats(client, List.of(idSeqKey));
                    this.stats.addClientRollbackStats(client, List.of(idSeqKey));
                    break;
            }
        }
    }

    @Override
    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        this.stats.addClientRollbackStats(client, idSeqKeys);
    }

    @Override
    public void collectInit(String queue, List<IdSeqKey> allItems) {
        if (!allItems.isEmpty()) {
            stats.initStats(queue, allItems);
        }
    }

    @Override
    public void markOldest(String queueName, IdSeqKey item) {
        this.stats.markOldest(queueName, item);
    }

    @Override
    public MBQStats getCollectedStats() {
        return this.stats;
    }
}