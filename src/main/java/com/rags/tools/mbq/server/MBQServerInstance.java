package com.rags.tools.mbq.server;

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
                return new MongoMBQServer(config);
            /*case LOCAL_HAZELCAST:
                return new MongoMBQServer(config);*/
            case REMOTE:
                return new MBQueueServerProxy(config);
            default:
                throw new MBQException("QueueTyoe is not configured");
        }
    }
}
