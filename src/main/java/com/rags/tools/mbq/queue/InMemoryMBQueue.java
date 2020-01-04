package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.util.HashingUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryMBQueue extends AbstractMBQueue {

    private final Map<String, Map<String, MBQMessage>> QUEUE_DS = new ConcurrentHashMap<>();

    @Override
    public MBQMessage get(String queueName, String id) {
        if (QUEUE_DS.containsKey(queueName)) {
            return QUEUE_DS.get(queueName).get(id);
        }
        return null;
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        if (QUEUE_DS.containsKey(queueName)) {
            Map<String, MBQMessage> queue = QUEUE_DS.get(queueName);
            return queue.values().parallelStream().filter(item -> status.contains(item.getStatus()) && item.getSeqKey().equals(seqKey)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<MBQMessage> pull(String queueName, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if (QUEUE_DS.containsKey(queueName)) {
            return ids.parallelStream().map(QUEUE_DS.get(queueName)::get).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<MBQMessage> mbqMessages = createMessages(messages, queueName);
        if (!QUEUE_DS.containsKey(queueName)) {
            QUEUE_DS.put(queueName, new ConcurrentHashMap<>());
        }
        mbqMessages.forEach(message -> QUEUE_DS.get(queueName).put(message.getId(), message));
        return mbqMessages;
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        if (!QUEUE_DS.containsKey(queueName)) {
            throw new MBQException("Queue doesn't exists " + queueName);
        }

        Map<String, MBQMessage> q = QUEUE_DS.get(queueName);
        ids.parallelStream().forEach(id -> {
            if (!q.containsKey(id)) {
                throw new MBQException("Client is not working on the item id " + id);
            }
        });

        ids.parallelStream().forEach(id -> q.get(id).updateStatus(status));
        return true;
    }
}
