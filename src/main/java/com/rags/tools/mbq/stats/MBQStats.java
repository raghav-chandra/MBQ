package com.rags.tools.mbq.stats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MBQStats {
    private Map<String, QueueStats> qStats = new ConcurrentHashMap<>();

    private Map<String, ClientStats> clientStats = new ConcurrentHashMap<>();

    public Collection<QueueStats> getQStats() {
        return qStats.values();
    }

    public Collection<String> getAllQueueNames() {
        return qStats.keySet();
    }

    public int getNoOfItemsInTheQueue(String queueName) {
        if (qStats.containsKey(queueName)) {
            return qStats.get(queueName).getSize();
        }
        return 0;
    }


    public Collection<ClientStats> getClientStats() {
        return clientStats.values();
    }

    public int getNoOfClientsConnected() {
        return clientStats.entrySet().size();
    }

    public int getConnectedClientIds() {
        return clientStats.entrySet().size();
    }

}
