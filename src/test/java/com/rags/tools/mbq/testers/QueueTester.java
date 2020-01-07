package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.QueueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class QueueTester extends MBQueueClient {

    protected QueueTester(QConfig config) {
        super(config);
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(QueueTester.class);

    public static int counter = 1;
    private static long startTime = 0;

    static void execute(int noOfClients, QueueType queueType) {
        List<QueueTester> clients = new ArrayList<>();
        for (int i = 0; i < noOfClients; i++) {
            clients.add(new QueueTester(new QConfig("localhost", 123, "RAGHAV", "" + i, 20, queueType)));
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                clients.forEach(client -> {
                    client.push(new QMessage((counter % clients.size()) + "DODA", counter + "BLAH BLAH" + (counter++ % clients.size())));
                    client.push(new QMessage((counter % clients.size()) + "DODA", counter + "BLAH BLAH" + (counter++ % clients.size())));
                    client.push(new QMessage((counter % clients.size()) + "DODA", counter + "BLAH BLAH" + (counter++ % clients.size())));
                    client.push(new QMessage((counter % clients.size()) + "DODA", counter + "BLAH BLAH" + (counter++ % clients.size())));
                });
            }
        }, 0, 30);

        clients.forEach(client -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.start();
            }
        }, 1));
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onMessage(List<Object> qItems) {
        qItems.forEach(it -> {
            LOCK.lock();
            CTR++;
            LOCK.unlock();

        });

        long timeTaken = System.currentTimeMillis() - startTime;
        float divider = timeTaken / 1000f;
        int ct = CTR;
        LOGGER.info("Completed {} no of messages in {} millis. Throughput per Sec : {}", ct, timeTaken, ct / divider);
    }

    private static int CTR = 0;
    private static final ReentrantLock LOCK = new ReentrantLock();
}
