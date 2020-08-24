package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryMBQDataStore extends AbstractMBQDataStore {

    private static InMemoryMBQDataStore INSTANCE;

    private final Map<String, Map<String, MBQMessage>> QUEUE_DS = new ConcurrentHashMap<>();

    public static synchronized InMemoryMBQDataStore getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new InMemoryMBQDataStore();
        }
        return INSTANCE;
    }

    @Override
    public List<MBQMessage> get(String queueName, List<String> ids) {
        if (QUEUE_DS.containsKey(queueName)) {
            return ids.stream().map(id -> QUEUE_DS.get(queueName).get(id)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<IdSeqKey>> getAllPendingIds() {
        return new HashMap<>();
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
            return true;
        }

        if (!QUEUE_DS.containsKey(queueName)) {
            return false;
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

    @Override
    public void updateStatus(QueueStatus prevStatus, QueueStatus newStatus) {

    }

    @Override
    public List<MBQMessage> search(SearchRequest searchRequest) {
        List<MBQMessage> messages = new LinkedList<>();
        if (searchRequest.getQueues() != null && !searchRequest.getQueues().isEmpty()) {
            searchRequest.getQueues().forEach(q -> {
                if (QUEUE_DS.containsKey(q)) {
                    Map<String, MBQMessage> allMessages = QUEUE_DS.get(q);
                    messages.addAll(filterMessages(new LinkedList<>(allMessages.values()), searchRequest));
                }
            });
        } else {
            List<MBQMessage> allMessages = new LinkedList<>(QUEUE_DS.values()).parallelStream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
            messages.addAll(filterMessages(allMessages, searchRequest));
        }
        return messages.stream().map(m -> new MBQMessage(m.getId(), m.getQueue(), m.getSeqKey(), m.getStatus(), null, m.getCreatedTimeStamp(), m.getUpdatedTimeStamp(), m.getScheduledAt())).collect(Collectors.toList());
    }

    private List<MBQMessage> filterMessages(List<MBQMessage> allMessages, SearchRequest searchRequest) {
        return allMessages.parallelStream().filter(msg -> {
            boolean matching = true;
            if (searchRequest.getSequence() != null) {
                matching = searchRequest.getSequence().equals(msg.getSeqKey());
            }

            if (searchRequest.getStatus() != null) {
                matching = matching && searchRequest.getStatus() == msg.getStatus();
            }

            if (searchRequest.getIds() != null) {
                matching = matching && searchRequest.getIds().contains(msg.getId());
            }
            return matching;
        }).collect(Collectors.toList());
    }
}