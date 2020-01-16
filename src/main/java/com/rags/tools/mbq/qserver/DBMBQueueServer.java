package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.queue.DBMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class DBMBQueueServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new DBMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    private static final DBMBQueueServer INSTANCE = new DBMBQueueServer(null);

    private DBMBQueueServer(QConfig config) {

    }

    public static MBQueueServer getInstance(QConfig.ServerConfig config) {
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
