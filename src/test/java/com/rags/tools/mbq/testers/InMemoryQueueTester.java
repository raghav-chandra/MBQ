package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

public class InMemoryQueueTester extends QueueTester {

    protected InMemoryQueueTester(QConfig config) {
        super(config);
    }

    public static void main(String[] args) {
        execute(5,QueueType.LOCAL_IN_MEMORY);
    }
}

