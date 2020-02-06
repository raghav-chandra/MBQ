package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

import static com.rags.tools.mbq.testers.QueueTester.execute;

public class InMemoryQueueTester {

    public static void main(String[] args) {
        QConfig.Builder configBuilder = new QConfig.Builder()
                .setBatch(5)
                .setPollingQueue("RAGHAV")
                .setDaemon(false)
                .setQueueType(QueueType.SINGLE_JVM_INMEMORY);
        execute(400, 20, configBuilder);
    }
}

