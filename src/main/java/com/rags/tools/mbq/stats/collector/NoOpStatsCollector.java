package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;
import java.util.Map;

public class NoOpStatsCollector implements MBQStatsCollector{

    @Override
    public void collectConnectedClients(Client client) {

    }

    @Override
    public void collectDisconnectedClients(Client client) {

    }

    @Override
    public void collectQueueStats(String queueName, QueueStatus status, int noOfItems) {

    }

    @Override
    public void collectQueueStats(String queueName, Map<QueueStatus, Integer> processed) {

    }

    @Override
    public void resetStats() {

    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {

    }
}
