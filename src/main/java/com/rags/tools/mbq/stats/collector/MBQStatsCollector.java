package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;

public interface MBQStatsCollector {

    void collectConnectedClients(Client client);

    void collectDisconnectedClients(Client client);

    void collectPendingStats(String queueName, int noOfItems);

//    void collectPendingStats(String queueName, Map<QueueStatus, Integer> processed);

    void resetStats();

    void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys);

    void collectClientProcessedStats(Client client, List<IdSeqKey> idSeqKeys);

    void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys);
}
