package com.rags.tools.mbq.queue.pending;

import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPendingIdSeqKeyQMap implements PendingQMap<IdSeqKey> {

    private static final Map<String, PendingQ<IdSeqKey>> QUEUE = new ConcurrentHashMap<>();

    @Override
    public PendingQ<IdSeqKey> get(String queueName) {
        if (!QUEUE.containsKey(queueName)) {
            QUEUE.put(queueName, new InMemoryPendingQ<>());
        }

        return QUEUE.get(queueName);
    }

}