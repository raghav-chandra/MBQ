package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueStats {
    private final String queueName;
    private int depth;

    private Map<QueueStatus, Long> processedItems = new ConcurrentHashMap<>();
    private final List<String> processing;

    public QueueStats(String queueName) {
        this.queueName = queueName;
        this.processing = new LinkedList<>();
    }

    public String getQueueName() {
        return queueName;
    }

    public int getDepth() {
        return depth;
    }

    public List<String> getProcessing() {
        return processing;
    }

    public Map<QueueStatus, Long> getProcessedItems() {
        return processedItems;
    }

    public void addProcessed(QueueStatus status, int added) {
        if (!processedItems.containsKey(status)) {
            processedItems.put(status, (long) added);
        } else {
            //TODO: Fix Procesing/PENDING->COMPLETED counter
            //Active Message processed
            if (status == QueueStatus.PROCESSING || status == QueueStatus.COMPLETED) {
                processedItems.put(QueueStatus.PENDING, processedItems.get(QueueStatus.PENDING) - added);
            }

            processedItems.put(status, processedItems.get(status) + (long) added);
        }

        this.depth += status == QueueStatus.COMPLETED ? -added : added;
    }

    public void addProcessed(Map<QueueStatus, Integer> processed) {
        processed.forEach(this::addProcessed);
    }

    public void addProcessing(List<String> ids) {
        this.processedItems.put(QueueStatus.PENDING, this.processedItems.get(QueueStatus.PENDING) - ids.size());
        this.processing.addAll(ids);
    }

    public void markCompleted(List<String> ids) {
        markItems(ids, QueueStatus.COMPLETED);
        this.depth -= ids.size();
    }

    public void markError(List<String> ids) {
        markItems(ids, QueueStatus.ERROR);
    }

    private void markItems(List<String> ids, QueueStatus status) {
        this.processedItems.put(status, this.processedItems.get(status) + ids.size());
        this.processing.removeAll(ids);
    }
}
