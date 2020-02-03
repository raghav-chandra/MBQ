package com.rags.tools.mbq.message;

public class QMessage {

    private static final String DEFAULT_SEQ = "raga-seq";

    private String seqKey;
    private byte[] message;
    private long scheduleAt;

    public QMessage() {
        this(null, null, 0);
    }

    public QMessage(String seqKey, byte[] message) {
        this(seqKey, message, 0);
    }

    public QMessage(String seqKey, byte[] message, long scheduleAt) {
        this.seqKey = seqKey;
        this.message = message;
        this.scheduleAt = scheduleAt;
    }

    public String getSeqKey() {
        return seqKey == null ? DEFAULT_SEQ : seqKey;
    }

    public byte[] getMessage() {
        return message;
    }

    public long getScheduleAt() {
        return scheduleAt;
    }

    protected void setSeqKey(String seqKey) {
        this.seqKey = seqKey;
    }

    protected void setMessage(byte[] message) {
        this.message = message;
    }

    public void setScheduleAt(long scheduleAt) {
        this.scheduleAt = scheduleAt;
    }
}
