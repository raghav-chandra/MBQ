package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;

public class MBQServerInstance {

    public static MBQueueServer createOrGet(QConfig config) {
        switch (config.getQueueType()) {
            case LOCAL_IN_MEMORY:
                return InMemoryMBQueueServer.getInstance();
            case LOCAL_RDB:
                return DBMBQueueServer.getInstance(config);
            case LOCAL_MONGO_DB:
                return MongoMBQServer.getInstance(config);
            case LOCAL_HAZELCAST:
                return HazelcastMBQServer.getInstance(config);
            case REMOTE:
                return new MBQueueServerProxy(config);
            default:
                throw new MBQException("QueueType is not configured");
        }
    }
}
