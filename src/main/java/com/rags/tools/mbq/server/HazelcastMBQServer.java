package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.queue.HazelcastMBQueue;
import com.rags.tools.mbq.queue.InMemoryMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class HazelcastMBQServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new HazelcastMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    private static final HazelcastMBQServer INSTANCE = new HazelcastMBQServer();

    private HazelcastMBQServer() {

    }

    public static MBQueueServer getInstance(QConfig config) {
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
