package com.rags.tools.mbq.connection.rest.messagecodec;

import com.rags.tools.mbq.QueueStatus;

import java.util.List;

public class SearchRequest {
    private List<String> ids;
    private String sequence;
    private QueueStatus status;
    private List<String> queues;

    public List<String> getIds() {
        return ids == null || ids.isEmpty() ? null : ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getSequence() {
        return sequence != null ? sequence.trim() : null;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<String> getQueues() {
        return queues;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public boolean isInValid() {
        return (ids == null || ids.isEmpty())
                && (queues == null || queues.isEmpty())
                && (sequence == null || sequence.trim().isEmpty())
                && status == null;
    }
}
