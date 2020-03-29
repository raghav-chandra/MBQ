package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.HazelcastMBQDataStore;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.queue.MBQDataStore;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.queue.pending.InMemoryPendingIdSeqKeyQMap;
import com.rags.tools.mbq.queue.pending.PendingQMap;

public class HazelcastMBQServer extends AbstractMBQueueServer {

    private static MBQueueServer INSTANCE;

    private HazelcastMBQServer(MBQDataStore mbqDataStore, PendingQMap<IdSeqKey> pendingQMap) {
        super(mbqDataStore, pendingQMap);
    }

    public synchronized static MBQueueServer getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = createAndInitialize(config);
        }
        return INSTANCE;
    }

    private static MBQueueServer createAndInitialize(QConfig.ServerConfig config) {
        validateConfig(config);
        return new HazelcastMBQServer(new HazelcastMBQDataStore(config), new InMemoryPendingIdSeqKeyQMap());
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
