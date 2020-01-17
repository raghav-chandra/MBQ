package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;

public class MBQServerInstance {

    public static MBQueueServer createOrGet(QConfig.ServerConfig config) {
        switch (config.getQueueType()) {
            case SINGLE_JVM_INMEMORY:
                return InMemoryMBQueueServer.getInstance();
            case SINGLE_JVM_RDB:
                return DBMBQueueServer.getInstance(config);
            case SINGLE_JVM_MONGO_DB:
                return MongoMBQServer.getInstance(config);
            case SINGLE_JVM_HAZELCAST:
                return HazelcastMBQServer.getInstance(config);
            case CENTRALIZED:
                return new MBQueueServerProxy(config);
            default:
                throw new MBQException("QueueType is not configured");
        }
    }
}
