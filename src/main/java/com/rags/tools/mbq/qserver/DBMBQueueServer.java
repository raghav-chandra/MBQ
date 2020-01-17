package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.DBMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class DBMBQueueServer extends AbstractMBQueueServer {

    private static MBQueueServer INSTANCE = null;

    private DBMBQueueServer(DBMBQueue dbmbQueue, PendingQueueMap pendingQueueMap) {
        super(dbmbQueue, pendingQueueMap);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new DBMBQueueServer(new DBMBQueue(config), new InMemoryPendingQueueMap());
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_RDB) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB queue with non DB QueueType");
        }

        if (config.getDbDriver() == null || config.getDbDriver().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB as QueueType but Driver class is empty");
        }

        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB as QueueType but DB URL is not provided");
        }

        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB as QueueType but username is not provided");
        }
    }

    @Override
    void init() {
        getQueue().updateStatus(QueueStatus.PROCESSING, QueueStatus.PENDING);
        getQueue().getAllPendingIds().forEach((key, val) -> getPendingQueueMap().get(key).addAll(val));
    }
}
