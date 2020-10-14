package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueType;

import static com.rags.tools.mbq.testers.QueueTester.execute;

public class CentralizedQueueTester {

    public static void main(String[] args) {
        QConfig.Builder configBuilder = new QConfig.Builder()
                .setBatch(5)
                .setPollingQueue("RAGHAV")
                .setQueueType(QueueType.CENTRALIZED)
                .setUrl("http://localhost:65000/");
        execute(1, 0, configBuilder);
    }
}
