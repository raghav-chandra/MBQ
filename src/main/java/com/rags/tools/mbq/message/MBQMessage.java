package com.rags.tools.mbq.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.rags.tools.mbq.QueueStatus;

import java.io.IOException;
import java.util.Objects;

public class MBQMessage extends QMessage implements DataSerializable {
    private String id;
    private QueueStatus status;
    private String queue;
    private long createdTimeStamp;
    private long updatedTimeStamp;

    public MBQMessage() {
    }

    public MBQMessage(String id, String queue, String seqKey, byte[] message) {
        this(id, queue, seqKey, QueueStatus.PENDING, message, System.currentTimeMillis(), 0, 0);
    }

    public MBQMessage(String id, String queue, String seqKey, byte[] message, long scheduledAt) {
        this(id, queue, seqKey, QueueStatus.PENDING, message, System.currentTimeMillis(), 0, scheduledAt);
    }

    public MBQMessage(String id, String queue, String seq, QueueStatus status, byte[] data, long createdTS, long updatedTS, long scheduledAt) {
        super(seq, data, scheduledAt == 0 ? createdTS : scheduledAt);
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

    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(status.name());
        out.writeUTF(queue);
        out.writeLong(createdTimeStamp);
        out.writeLong(updatedTimeStamp);
        out.writeLong(getScheduledAt());
        out.writeUTF(getSeqKey());
        out.writeByteArray(getMessage());
    }

    public void readData(ObjectDataInput in) throws IOException {
        this.id = in.readUTF();
        this.status = QueueStatus.valueOf(in.readUTF());
        this.queue = in.readUTF();
        this.createdTimeStamp = in.readLong();
        this.updatedTimeStamp = in.readLong();
        this.setScheduledAt(in.readLong());
        this.setSeqKey(in.readUTF());
        this.setMessage(in.readByteArray());
    }

    @JsonIgnore
    public ProcessingItem getProcessingItem() {
        return new ProcessingItem(id, getMessage(), status);
    }

    public static class ProcessingItem {
        private String id;
        private byte[] message;

        private QueueStatus queueStatus;

        public ProcessingItem(String id, byte[] message, QueueStatus queueStatus) {
            this.id = id;
            this.message = message;
            this.queueStatus = queueStatus;
        }

        public String getId() {
            return id;
        }

        public byte[] getMessage() {
            return message;
        }

        public QueueStatus getQueueStatus() {
            return queueStatus;
        }
    }
}
