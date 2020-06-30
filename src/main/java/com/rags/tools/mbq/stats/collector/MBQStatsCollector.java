package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;

public interface MBQStatsCollector {

    void addConnectedClient(Client client);

    void increment(QueueStatus status, int items);

    void resetStats();
}
