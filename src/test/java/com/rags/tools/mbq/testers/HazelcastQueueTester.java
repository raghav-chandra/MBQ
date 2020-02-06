package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

import static com.rags.tools.mbq.testers.QueueTester.execute;

public class HazelcastQueueTester {

    public static void main(String[] args) {
        QConfig.Builder configBuilder = new QConfig.Builder()
                .setBatch(5)
                .setUrl("localhost")
                .setUser("raghav")
                .setPassword("chandra")
                .setPollingQueue("RAGHAV")
                .setDaemon(false)
                .setQueueType(QueueType.SINGLE_JVM_HAZELCAST);
        execute(32, 4, configBuilder);
    }
}

