package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.queue.IdSeqKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QueueStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueStats.class);

    private int depth;
    private int pending;
    private int processed;
    private int rolledBack;
    private IdSeqKey oldestItem;

    private final String queueName;
    private final List<String> errors;
    private final List<String> processing;

    private int offset;

    public QueueStats(String queueName) {
        this.queueName = queueName;
        this.processing = new LinkedList<>();
        this.errors = new LinkedList<>();
    }

    public void addPending(int added) {
        if (this.offset <= 0) {
            this.offset += added;
            this.offset = Math.min(this.offset, 0);
        }
        this.pending += added;
        this.pending = Math.max(this.pending, 0);
        this.depth = calculateDepth();
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}, RolledBack : {}", queueName, depth, pending, processing.size(), processed, errors.size(), rolledBack);
    }

    private int calculateDepth() {
        return pending + errors.size() + processing.size();
    }

    public void addProcessing(List<String> ids) {
        int offset = this.pending - ids.size();
        if (offset < 0) {
            this.pending = 0;
            this.offset += offset;
        } else {
            this.pending -= ids.size();
        }
        this.processing.addAll(ids);
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}, RolledBack : {}", queueName, depth, pending, processing.size(), processed, errors.size(), rolledBack);
    }

    public void markCompleted(List<String> ids) {
        this.processed += ids.size();
        this.processing.removeAll(ids);
        this.depth = calculateDepth();
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}, RolledBack : {}", queueName, depth, pending, processing.size(), processed, errors.size(), rolledBack);
    }

    public void markRolledBack(List<String> processingIds) {
        this.pending += processingIds.size();
        this.processing.removeAll(processingIds);
        this.rolledBack += processingIds.size();
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}, RolledBack : {}", queueName, depth, pending, processing.size(), processed, errors.size(), rolledBack);
    }

    public void markError(List<String> ids) {
        this.errors.addAll(ids);
        this.processing.removeAll(ids);
        LOGGER.debug("Queue Name : {}. Stats -> Depth : {}, Pending : {}, Processing : {}, Completed :{}, Errored :{}, RolledBack : {}", queueName, depth, pending, processing.size(), processed, errors.size(), rolledBack);
    }

    public void markOldest(IdSeqKey idSeqKey) {
        this.oldestItem = idSeqKey;
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

    public int getRolledBack() {
        return rolledBack;
    }

    public IdSeqKey getOldestItem() {
        return oldestItem;
    }

    public String getQueueName() {
        return queueName;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getProcessing() {
        return processing;
    }
}
