package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.util.HashingUtil;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMBQueue implements MBQueue {

    protected MBQMessage createMessage(QMessage message, String queueName) {
        long currTime = System.nanoTime();
        String id = currTime + HashingUtil.hashSHA256(message.getSeqKey() + currTime);
        return new MBQMessage(id,queueName,  message.getSeqKey(), message.getMessage());
    }

    protected List<MBQMessage> createMessages(List<QMessage> messages, String queueName) {
        return messages.stream().map(message -> {
            long currTime = System.nanoTime();
            String id = currTime + HashingUtil.hashSHA256(message.getSeqKey() + currTime);
            return new MBQMessage(id, queueName, message.getSeqKey(), message.getMessage());
        }).collect(Collectors.toList());
    }
}
