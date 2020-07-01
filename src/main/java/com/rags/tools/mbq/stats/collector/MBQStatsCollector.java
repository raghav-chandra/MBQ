package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;
import java.util.Map;

public interface MBQStatsCollector {

    void collectConnectedClients(Client client);

    void collectDisconnectedClients(Client client);

    void collectQueueStats(String queueName, QueueStatus status, int noOfItems);

    void collectQueueStats(String queueName, Map<QueueStatus, Integer> processed);

    void resetStats();

    void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys);
}
