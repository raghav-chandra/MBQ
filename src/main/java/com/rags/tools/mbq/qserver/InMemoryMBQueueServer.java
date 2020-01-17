package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.DBMBQueue;
import com.rags.tools.mbq.queue.InMemoryMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class InMemoryMBQueueServer extends AbstractMBQueueServer {

    private static MBQueueServer INSTANCE;

    public InMemoryMBQueueServer(MBQueue mbQueue, PendingQueueMap pendingQueueMap) {
        super(mbQueue, pendingQueueMap);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new InMemoryMBQueueServer(new InMemoryMBQueue(), new InMemoryPendingQueueMap());
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_INMEMORY) {
            throw new MBQException("Wrong configuration passed. You are trying to setup In Memory queue with other QueueType");
        }
    }

    @Override
    void init() {
        getQueue().updateStatus(QueueStatus.PROCESSING, QueueStatus.PENDING);
        getQueue().getAllPendingIds().forEach((key, val) -> getPendingQueueMap().get(key).addAll(val));
    }
}
