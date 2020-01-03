package com.rags.tools.mbq.queue.pending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPendingQueueMap implements PendingQueueMap {

    private static final Map<String, PendingQueue<String>> QUEUE = new ConcurrentHashMap<>();

    @Override
    public PendingQueue<String> get(String queueName) {
        if (!QUEUE.containsKey(queueName)) {
            QUEUE.put(queueName, new InMemoryPendingQueue<>());
        }

        return QUEUE.get(queueName);
    }
}
