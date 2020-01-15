package com.rags.tools.mbq.message;

public class QMessage {

    private static final String DEFAULT_SEQ = "raga-seq";

    private final String seqKey;
    private final byte[] message;

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
}
