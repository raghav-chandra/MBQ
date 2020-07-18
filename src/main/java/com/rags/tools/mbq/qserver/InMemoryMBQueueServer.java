package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.InMemoryMBQDataStore;
import com.rags.tools.mbq.queue.MBQDataStore;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.MBQStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryMBQueueServer extends AbstractMBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryMBQueueServer.class);

    private static MBQueueServer INSTANCE;

    public InMemoryMBQueueServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsService) {
        super(mbqDataStore, pendingQMap, statsService);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new InMemoryMBQueueServer(new InMemoryMBQDataStore(), new InMemoryPendingIdSeqKeyQMap(), MBQStatsService.getInstance(config.getStatsCollectorClass()));
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_INMEMORY) {
            throw new MBQException("Wrong configuration passed. You are trying to setup In Memory queue with other QueueType");
        }
    }
}
