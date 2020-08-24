package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.store.MBQDataStore;
import com.rags.tools.mbq.queue.store.MongoMBQDataStore;
import com.rags.tools.mbq.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.MBQStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoQueueServer extends MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueueServer.class);

    private static QueueServer INSTANCE = null;

    public MongoQueueServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsService) {
        super(mbqDataStore, pendingQMap, statsService);
    }

    public synchronized static QueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static QueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new MongoQueueServer(new MongoMBQDataStore(config), new InMemoryPendingIdSeqKeyQMap(), MBQStatsService.getInstance(config.getStatsCollectorClass()));
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
}
