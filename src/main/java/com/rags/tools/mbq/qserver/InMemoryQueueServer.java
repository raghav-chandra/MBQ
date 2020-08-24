package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.store.InMemoryMBQDataStore;
import com.rags.tools.mbq.queue.store.MBQDataStore;
import com.rags.tools.mbq.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.MBQStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryQueueServer extends MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryQueueServer.class);

    private static QueueServer INSTANCE;

    public InMemoryQueueServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsService) {
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
        return new InMemoryQueueServer(new InMemoryMBQDataStore(), new InMemoryPendingIdSeqKeyQMap(), MBQStatsService.getInstance(config.getStatsCollectorClass()));
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_INMEMORY) {
            throw new MBQException("Wrong configuration passed. You are trying to setup In Memory queue with other QueueType");
        }
    }
}
