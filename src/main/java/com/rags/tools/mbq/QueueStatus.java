package com.rags.tools.mbq;

public enum QueueStatus {
    PENDING, PROCESSING, COMPLETED, ERROR, HELD, BLOCKED;

    public boolean isBlocking() {
        return this == ERROR || this == HELD || this == BLOCKED;
    }
}
