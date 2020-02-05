package com.rags.tools.mbq.queue.pending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPendingQMap implements PendingQMap<String> {

    private static final Map<String, PendingQ<String>> QUEUE = new ConcurrentHashMap<>();

    @Override
    public PendingQ<String> get(String queueName) {
        if (!QUEUE.containsKey(queueName)) {
            QUEUE.put(queueName, new InMemoryPendingQ<>());
        }

        return QUEUE.get(queueName);
    }
}
