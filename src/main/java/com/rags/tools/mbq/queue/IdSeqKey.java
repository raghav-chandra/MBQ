package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.exception.MBQException;

import java.util.Objects;

public class IdSeqKey implements Cloneable {
    private final String id;
    private final String seqKey;
    private final long scheduledAt;
    private QueueStatus status;

    public IdSeqKey(String id, String seqKey, QueueStatus status, long scheduledAt) {
        this.id = id;
        this.seqKey = seqKey;
        this.status = status;
        this.scheduledAt = scheduledAt;
    }

    public String getId() {
        return id;
    }

    public String getSeqKey() {
        return seqKey;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdSeqKey)) return false;
        IdSeqKey idSeqKey = (IdSeqKey) o;
        return Objects.equals(id, idSeqKey.id) &&
                Objects.equals(seqKey, idSeqKey.seqKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seqKey);
    }

    public IdSeqKey setStatus(QueueStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public IdSeqKey clone() {
        try {
            return (IdSeqKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new MBQException("Exception occurred while processing request", e);
        }
    }
}
