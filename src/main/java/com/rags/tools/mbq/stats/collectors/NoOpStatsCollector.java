package com.rags.tools.mbq.stats.collectors;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;

public class NoOpStatsCollector implements MBQStatsCollector{

    @Override
    public void collectConnectedClients(Client client) {

    }

    @Override
    public void collectDisconnectedClients(Client client) {

    }

    @Override
    public void collectPendingStats(String queueName, int noOfItems) {

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
}
