package com.rags.tools.mbq.queue;

import java.util.Objects;

public class IdSeqKey {
    private final String id;
    private final String seqKey;

    public IdSeqKey(String id, String seqKey) {
        this.id = id;
        this.seqKey = seqKey;
    }

    public String getId() {
        return id;
    }

    public String getSeqKey() {
        return seqKey;
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
}
