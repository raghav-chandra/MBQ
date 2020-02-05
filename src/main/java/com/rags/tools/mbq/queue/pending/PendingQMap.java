package com.rags.tools.mbq.queue.pending;

public interface PendingQMap<T> {

    /**
     * Gets pending Queue containing list of items.
     *
     * @param queueName queue Name
     * @return Pending Queue
     */
    PendingQ<T> get(String queueName);
}
