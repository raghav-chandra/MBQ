package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;

public class MBQDataStoreInstance {
    public static MBQDataStore createOrGet(QConfig.ServerConfig config) {
        switch (config.getQueueType()) {
            case SINGLE_JVM_INMEMORY:
                return InMemoryMBQDataStore.getInstance(config);
            case SINGLE_JVM_RDB:
                return RDBMBQDataStore.getInstance(config);
            case SINGLE_JVM_MONGO_DB:
                return MongoMBQDataStore.getInstance(config);
            case SINGLE_JVM_HAZELCAST:
                return HazelcastMBQDataStore.getInstance(config);
            default:
                throw new MBQException("QueueType is not configured to create Data Store");
        }
    }
}
