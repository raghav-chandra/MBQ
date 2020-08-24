package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.store.HazelcastMBQDataStore;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.store.MBQDataStore;
import com.rags.tools.mbq.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.MBQStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastQueueServer extends MBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastQueueServer.class);

    private static QueueServer INSTANCE;

    private HazelcastQueueServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsService statsService) {
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
        return new HazelcastQueueServer(new HazelcastMBQDataStore(config), new InMemoryPendingIdSeqKeyQMap(), MBQStatsService.getInstance(config.getStatsCollectorClass()));
    }

    private static void validateConfig(QConfig.ServerConfig config) {
        if (config.getQueueType() != QueueType.SINGLE_JVM_HAZELCAST) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB queue with non DB QueueType");
        }

        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB as QueueType but Hazelcast URL is not provided");
        }

        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new MBQException("Wrong configuration passed. You are trying to setup DB as QueueType but username is not provided");
        }
    }
}
