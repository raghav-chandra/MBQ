package com.rags.tools.mbq.queue.pending;

public interface PendingQueueMap {

    /**
     * Gets pending Queue containing list of items.
     *
     * @param queueName queue Name
     * @return Pending Queue
     */
    PendingQueue<String> get(String queueName);
}
