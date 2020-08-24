package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;

public class MBQServerInstance {

    public static QueueServer createOrGet(QConfig.ServerConfig config) {
        switch (config.getQueueType()) {
            case SINGLE_JVM_INMEMORY:
                return InMemoryQueueServer.getInstance(config);
            case SINGLE_JVM_RDB:
                return RDBQueueServer.getInstance(config);
            case SINGLE_JVM_MONGO_DB:
                return MongoQueueServer.getInstance(config);
            case SINGLE_JVM_HAZELCAST:
                return HazelcastQueueServer.getInstance(config);
            case CENTRALIZED:
                return new QueueServerProxy(config);
            default:
                throw new MBQException("QueueType is not configured");
        }
    }
}
