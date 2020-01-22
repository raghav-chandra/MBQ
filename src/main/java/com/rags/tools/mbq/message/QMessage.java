package com.rags.tools.mbq.message;

public class QMessage {

    private static final String DEFAULT_SEQ = "raga-seq";

    private String seqKey;
    private byte[] message;

    public QMessage() {

    }

    public QMessage(String seqKey, byte[] message) {
        this.seqKey = seqKey;
        this.message = message;
    }

    public String getSeqKey() {
        return seqKey == null ? DEFAULT_SEQ : seqKey;
    }

    public byte[] getMessage() {
        return message;
    }

    protected void setSeqKey(String seqKey) {
        this.seqKey = seqKey;
    }

    protected void setMessage(byte[] message) {
        this.message = message;
    }
}
