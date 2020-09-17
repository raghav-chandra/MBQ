package com.rags.tools.mbq.stats.collectors;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.MBQStats;

import java.util.List;

public class NoOpStatsCollector implements MBQStatsCollector {

    @Override
    public void collectConnectedClients(Client client) {

    }

    @Override
    public void collectDisconnectedClients(Client client) {

    }

    @Override
    public void collectPendingStats(Client client, String queueName, int noOfItems) {

    }

    @Override
    public void resetStats() {

    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {

    }

    @Override
    public void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {

    }

    @Override
    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {

    }

    @Override
    public void markOldest(String queueName, IdSeqKey item) {

    }

    @Override
    public MBQStats getCollectedStats() {
        return new MBQStats();
    }

    @Override
    public void collectInit(String queue, List<IdSeqKey> allItems) {

    }
}
