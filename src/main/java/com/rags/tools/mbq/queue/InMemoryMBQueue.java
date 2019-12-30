package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMBQueue implements MBQueue {

    private final Map<String, Map<String, LinkedList<MBQMessage>>> QUEUE_DS = new ConcurrentHashMap<>();

    @Override
    public List<MBQMessage> pull(Client client) {
        return null;
    }

    @Override
    public List<MongoMBQueue> push(Client client, List<MBQueue> messages) {
        return null;
    }

    @Override
    public boolean updateStatus(Client client, List<String> ids, QueueStatus status) {
        return false;
    }
}
