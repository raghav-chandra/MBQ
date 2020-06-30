package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;

public class NoOpStatsCollector implements MBQStatsCollector{

    @Override
    public void addConnectedClient(Client client) {

    }

    @Override
    public void increment(String queueName, QueueStatus status, int items) {

    }

    @Override
    public void resetStats() {

    }

    @Override
    public void removeConnectedClient(Client client) {

    }
}
