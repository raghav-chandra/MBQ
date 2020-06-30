package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStats {
    private final String id;
    private final Map<QueueStatus, Long> itemsProcessed = new ConcurrentHashMap<>();

    public ClientStats(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Map<QueueStatus, Long> getItemsProcessed() {
        return itemsProcessed;
    }
}
