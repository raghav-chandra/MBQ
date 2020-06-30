package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueStats {
    private String queueName;
    private int size;
    private Map<QueueStatus, Long> itemsProcessed = new ConcurrentHashMap<>();

    public QueueStats(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<QueueStatus, Long> getItemsProcessed() {
        return itemsProcessed;
    }

    public void addProcessed(QueueStatus status, int added) {
        if (!itemsProcessed.containsKey(status)) {
            itemsProcessed.put(status, (long) added);
        } else {
            itemsProcessed.put(status, itemsProcessed.get(status) + (long) added);
        }
        size += added;
    }

    public void addProcessed(Map<QueueStatus, Integer> processed) {
        processed.forEach(this::addProcessed);
    }
}
