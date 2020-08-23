package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.store.DBMBQueueDataStore;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.MBQStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBMBQueueServer extends AbstractMBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBMBQueueServer.class);

    private static MBQueueServer INSTANCE = null;

    private DBMBQueueServer(DBMBQueueDataStore dbmbQueueDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsCollector) {
        super(dbmbQueueDataStore, pendingQMap, statsCollector);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new DBMBQueueServer(new DBMBQueueDataStore(config), new InMemoryPendingIdSeqKeyQMap(), MBQStatsService.getInstance(config.getStatsCollectorClass()));
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
}
