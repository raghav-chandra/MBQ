package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

public class DBQueueTester extends QueueTester {
    protected DBQueueTester(QConfig config) {
        super(config);
    }

    public static void main(String[] args) {
        execute(1,4,  QueueType.SINGLE_JVM_RDB);
    }
}

