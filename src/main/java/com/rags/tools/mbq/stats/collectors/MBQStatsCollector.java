package com.rags.tools.mbq.stats.collectors;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.MBQStats;

import java.util.List;

public interface MBQStatsCollector {

    void collectConnectedClients(Client client);

    void collectDisconnectedClients(Client client);

    void collectPendingStats(Client client, String queueName, int noOfItems);

    void resetStats();

    void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys);

    void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys);

    void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys);

    void markOldest(String queueName, IdSeqKey item);

    MBQStats getCollectedStats();

    void collectInit(String queue, List<IdSeqKey> allItems);
}
