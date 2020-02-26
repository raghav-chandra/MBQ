package com.rags.tools.mbq.message;

public class QMessage {

    private String seqKey;
    private byte[] message;
    private long scheduledAt;

    public QMessage() {
        this(null, null, 0);
    }

    public QMessage(String seqKey, byte[] message) {
        this(seqKey, message, 0);
    }

    public QMessage(String seqKey, byte[] message, long scheduleAt) {
        this.seqKey = seqKey;
        this.message = message;
        this.scheduledAt = scheduleAt;
    }

    public String getSeqKey() {
        return seqKey;
    }

    public byte[] getMessage() {
        return message;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }

    protected void setSeqKey(String seqKey) {
        this.seqKey = seqKey;
    }

    protected void setMessage(byte[] message) {
        this.message = message;
    }

    public void setScheduledAt(long scheduleAt) {
        this.scheduledAt = scheduleAt;
    }
}
