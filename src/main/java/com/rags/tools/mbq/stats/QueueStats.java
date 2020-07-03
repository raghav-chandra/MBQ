package com.rags.tools.mbq.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QueueStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueStats.class);

    private int depth;
    private int pending;
    private int processed;

    private final String queueName;
    private final List<String> errors;
    private final List<String> processing;

    public QueueStats(String queueName) {
        this.queueName = queueName;
        this.processing = new LinkedList<>();
        this.errors = new LinkedList<>();
    }

    public void addPending(int added) {
        this.pending += added;
        this.depth += added;
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}", queueName, depth, pending, processing.size(), processed, errors.size());
    }

    public void addProcessing(List<String> ids) {
        this.pending -= ids.size();
        this.processing.addAll(ids);
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}", queueName, depth, pending, processing.size(), processed, errors.size());
    }

    public void markCompleted(List<String> ids) {
        this.processed += ids.size();
        this.processing.removeAll(ids);
        this.depth -= ids.size();
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}", queueName, depth, pending, processing.size(), processed, errors.size());
    }

    public void markRolledBack(List<String> processingIds) {
        this.pending += processingIds.size();
        this.processing.removeAll(processingIds);
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}", queueName, depth, pending, processing.size(), processed, errors.size());
    }

    public void markError(List<String> ids) {
        this.errors.addAll(ids);
        this.processing.removeAll(ids);
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}", queueName, depth, pending, processing.size(), processed, errors.size());
    }

    public String getQueueName() {
        return queueName;
    }

    public int getDepth() {
        return depth;
    }

    public int getPending() {
        return pending;
    }

    public int getProcessed() {
        return processed;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getProcessing() {
        return processing;
    }
}
