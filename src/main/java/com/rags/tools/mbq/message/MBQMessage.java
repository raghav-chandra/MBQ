package com.rags.tools.mbq.message;

import com.rags.tools.mbq.QueueStatus;

import java.util.Objects;

public class MBQMessage extends QMessage {
    private String id;
    private QueueStatus status;
    private long createdTimeStamp;
    private long updatedTimeStamp;

    public MBQMessage(String id, String seqKey, Object message) {
        super(seqKey, message);
        this.id = id;
        this.status = QueueStatus.PENDING;
        this.createdTimeStamp = System.currentTimeMillis();
        this.updatedTimeStamp = this.createdTimeStamp;
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
