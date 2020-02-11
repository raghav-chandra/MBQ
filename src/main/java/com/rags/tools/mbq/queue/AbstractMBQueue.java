package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.util.HashingUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMBQueue implements MBQueue {


    @Override
    public MBQMessage get(String queueName, String id) {
        List<MBQMessage> messages = get(queueName, Collections.singletonList(id));
        return messages.isEmpty() ? null : messages.get(0);
    }

    protected MBQMessage createMessage(QMessage message, String queueName) {
        long currTime = System.currentTimeMillis();
        String id = currTime + HashingUtil.hashSHA256(System.nanoTime() + message.getSeqKey() + currTime);
        return new MBQMessage(id, queueName, message.getSeqKey(), message.getMessage());
    }

    protected List<MBQMessage> createMessages(List<QMessage> messages, String queueName) {
        return messages.stream().map(message -> {
            long currTime = System.currentTimeMillis();
            String id = currTime + HashingUtil.hashSHA256(System.nanoTime() + message.getSeqKey() + currTime);
            return new MBQMessage(id, queueName, message.getSeqKey(), message.getMessage());
        }).collect(Collectors.toList());
    }
}
