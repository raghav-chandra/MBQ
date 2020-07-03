package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.HazelcastMBQDataStore;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.MBQDataStore;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;
import com.rags.tools.mbq.stats.collector.MBQStatsCollector;
import com.rags.tools.mbq.stats.collector.NoOpStatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastMBQServer extends AbstractMBQueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastMBQServer.class);

    private static MBQueueServer INSTANCE;

    private HazelcastMBQServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap, MBQStatsCollector statsCollector) {
        super(mbqDataStore, pendingQMap, statsCollector);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);

        MBQStatsCollector statsCollector = getStatsCollector(config.getStatsCollectorClass());

        return new HazelcastMBQServer(new HazelcastMBQDataStore(config), new InMemoryPendingIdSeqKeyQMap(), statsCollector);
    }

    private static MBQStatsCollector getStatsCollector(String statsCollectorClass) {
        MBQStatsCollector statsCollector = new NoOpStatsCollector();
        try {
            if (statsCollectorClass != null && !statsCollectorClass.isBlank()) {
                Class<?> aClass = Class.forName(statsCollectorClass);
                statsCollector = (MBQStatsCollector) aClass.getConstructor().newInstance();

            }
        } catch (Throwable throwable) {
            LOGGER.warn("Failed while loading class {} for Stats collector. Falling back to NoOpsStatsCollector", statsCollectorClass);
        }
        return statsCollector;
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
