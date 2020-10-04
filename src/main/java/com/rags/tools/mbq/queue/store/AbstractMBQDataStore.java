package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.util.HashingUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractMBQDataStore implements MBQDataStore {


    @Override
    public MBQMessage get(String queueName, String id) {
        List<MBQMessage> messages = get(queueName, Collections.singletonList(id));
        return messages.isEmpty() ? null : messages.get(0);
    }

    protected List<MBQMessage> createMessages(List<QMessage> messages, String queueName) {
        AtomicInteger counter = new AtomicInteger(0);
        return messages.stream().map(message -> {
            long currTime = System.currentTimeMillis();
            String id = currTime + (counter.getAndIncrement() + HashingUtil.hashSHA256(System.nanoTime() + message.getSeqKey() + currTime));
            return new MBQMessage(id, queueName, message.getSeqKey(), message.getMessage());
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(String queueName, Map<QueueStatus, List<String>> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        ids.forEach((status, id) -> updateStatus(queueName, id, status));

        return true;
    }
}
