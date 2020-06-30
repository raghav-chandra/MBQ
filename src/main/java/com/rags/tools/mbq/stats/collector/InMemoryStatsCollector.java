package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.stats.MBQStats;

public class InMemoryStatsCollector implements MBQStatsCollector {

    private final MBQStats stats = new MBQStats();

    @Override
    public void addConnectedClient(Client client) {

    }

    @Override
    public void increment(QueueStatus status, int items) {

    }

    @Override
    public void resetStats() {

    }
}
