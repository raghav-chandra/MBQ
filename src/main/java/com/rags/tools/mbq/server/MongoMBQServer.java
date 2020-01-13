package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.MongoMBQueue;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class MongoMBQServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new MongoMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    private MongoMBQServer(QConfig config) {

    }

    public static MBQueueServer getInstance(QConfig config) {
        return new MongoMBQServer(config);
    }

    @Override
    void init() {
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
