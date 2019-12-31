package com.rags.tools.mbq.message;

public class QMessage {

    private static final String DEFAULT_SEQ = "raga-seq";

    private final String seqKey;
    private final Object message;

    public QMessage(String seqKey, Object message) {
        this.seqKey = seqKey;
        this.message = message;
    }

    public String getSeqKey() {
        return seqKey == null ? DEFAULT_SEQ : seqKey;
    }

    public Object getMessage() {
        return message;
    }
}
