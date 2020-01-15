package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.queue.InMemoryMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class InMemoryMBQueueServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new InMemoryMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    private static final InMemoryMBQueueServer INSTANCE = new InMemoryMBQueueServer();

    private InMemoryMBQueueServer() {

    }

    public static MBQueueServer getInstance() {
        return INSTANCE;
    }

    @Override
    void init() {
        QUEUE.updateStatus(QueueStatus.PROCESSING, QueueStatus.PENDING);
        QUEUE.getAllPendingIds().forEach((key, val) -> ALL_PENDING_MESSAGES.get(key).addAll(val));
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
