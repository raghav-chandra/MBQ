package com.rags.tools.mbq.server;

import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.queue.InMemoryMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

import java.util.List;
import java.util.Map;

public class InMemoryMBQueueServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new InMemoryMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    @Override
    void init() {
        QUEUE.getAllPendingIds().forEach((key, val)-> ALL_PENDING_MESSAGES.get(key).addAll(val));
    }

    @Override
    protected MBQueue getQueue() {
        return QUEUE;
    }

    @Override
    protected PendingQueueMap getPendingQueueMap() {
        return ALL_PENDING_MESSAGES;
    }
}
