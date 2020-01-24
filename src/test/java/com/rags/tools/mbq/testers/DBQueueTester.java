package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.queue.QueueType;

import static com.rags.tools.mbq.testers.QueueTester.execute;

public class DBQueueTester {

    public static void main(String[] args) {
        QConfig.Builder configBuilder = new QConfig.Builder()
                .setBatch(10)
                .setPollingQueue("RAGHAV")
                .setQueueType(QueueType.SINGLE_JVM_RDB)
                .setDbDriver("com.mysql.cj.jdbc.Driver")
                .setUrl("jdbc:mysql://localhost:3306/raga")
                .setUser("raga")
                .setPassword("raga");
        execute(4, 0, configBuilder);
    }
}

