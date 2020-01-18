package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.DBMBQueue;
import com.rags.tools.mbq.queue.MBQueue;
import com.rags.tools.mbq.queue.MongoMBQueue;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;

public class MongoMBQServer extends AbstractMBQueueServer {

    private static MBQueueServer INSTANCE = null;

    public MongoMBQServer(MBQueue mbQueue, PendingQueueMap pendingQueueMap) {
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
        return new MongoMBQServer(new MongoMBQueue(config), new InMemoryPendingQueueMap());
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_MONGO_DB) {
            throw new MBQException("Wrong configuration passed. You are trying to setup Mongo DB queue with other QueueType");
        }

        if (config.getDbDriver() == null || config.getDbDriver().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup Mongo DB as QueueType but Driver class is empty");
        }

        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup Mongo DB as QueueType but DB URL is not provided");
        }

        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup Mongo DB as QueueType but username is not provided");
        }
    }

    @Override
    void init() {
        getQueue().getAllPendingIds().forEach((key, val) -> getPendingQueueMap().get(key).addAll(val));
    }
}