package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.client.MBQueuePublisher;
import com.rags.tools.mbq.message.QMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class QueueTester extends MBQueueClient {

    protected QueueTester(QConfig config) {
        super(config);
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(QueueTester.class);

    public static int counter = 1;
    private static long startTime = 0;

    static void execute(int publishers, int consumers, QConfig.Builder config) {
        List<MBQueuePublisher> allPublishers = new ArrayList<>();
        for (int i = 0; i < publishers; i++) {
            allPublishers.add(new MBQueuePublisher(config.clone().setWorkerName("publisher-" + i).create()));
        }

        List<QueueTester> allConsumers = new ArrayList<>();
        for (int i = 0; i < consumers; i++) {
            allConsumers.add(new QueueTester(config.clone().setWorkerName("consumer-" + i).create()));
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                allPublishers.forEach(client -> {
                    List<QMessage> messages = new LinkedList<>();
                    for (int i = 0; i < 10; i++) {
                        messages.add(new QMessage((counter % allPublishers.size()) + "DODA", (counter + "BLAH BLAH" + (counter++ % allPublishers.size())).getBytes()));
                    }
                    client.push(messages);
                });
            }
        }, 0, 100);

        allPublishers.forEach(client -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.start();
            }
        }, 1));

        allConsumers.forEach(client -> new Timer().schedule(new TimerTask() {
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
        System.out.println("Completed " + ct + " no of messages in " + timeTaken + " millis. Throughput per Sec : " + ct / divider);
        LOGGER.info("Completed {} no of messages in {} millis. Throughput per Sec : {}", ct, timeTaken, ct / divider);
    }

    private static int CTR = 0;
    private static final ReentrantLock LOCK = new ReentrantLock();
}
