package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

import static com.rags.tools.mbq.testers.QueueTester.execute;

public class CentralizedQueueTester {

    public static void main(String[] args) {
        QConfig.Builder configBuilder = new QConfig.Builder()
                .setBatch(10)
                .setPollingQueue("RAGHAV")
                .setQueueType(QueueType.CENTRALIZED)
                .setUrl("http://localhost:65000/")
                .setValidationQuery("select 1")
                .setUser("raga")
                .setMaxxConn(10)
                .setPassword("raga");
        execute(1, 1, configBuilder);
    }
}
