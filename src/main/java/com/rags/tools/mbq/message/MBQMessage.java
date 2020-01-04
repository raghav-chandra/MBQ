package com.rags.tools.mbq.message;

import com.rags.tools.mbq.QueueStatus;

import java.util.Objects;

public class MBQMessage extends QMessage {
    private String id;
    private QueueStatus status;
    private String queue;
    private long createdTimeStamp;
    private long updatedTimeStamp;

    public MBQMessage(String id, String queue, String seqKey, Object message) {
        this(id, queue, seqKey, QueueStatus.PENDING, message, System.currentTimeMillis(), 0);
    }

    public MBQMessage(String id, String queue, String seq, QueueStatus status, Object data, long createdTS, long updatedTS) {
        super(seq, data);
        this.id = id;
        this.queue = queue;
        this.status = status;
        this.createdTimeStamp = createdTS;
        this.updatedTimeStamp = updatedTS;
    }

    public String getId() {
        return id;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public long getUpdatedTimeStamp() {
        return updatedTimeStamp;
    }

    public void updateStatus(QueueStatus status) {
        if (status != null) {
            this.status = status;
            this.updatedTimeStamp = System.currentTimeMillis();
        }
    }

    public String getQueue() {
        return queue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MBQMessage)) return false;
        MBQMessage that = (MBQMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
