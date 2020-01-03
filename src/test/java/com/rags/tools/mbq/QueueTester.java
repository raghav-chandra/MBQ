package com.rags.tools.mbq;

import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.message.QMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class QueueTester extends MBQueueClient {

    protected QueueTester(QConfig config) {
        super(config);
    }


    public static final Logger LOGGER = LoggerFactory.getLogger(QueueTester.class);
    public static int counter = 1;

    private static long startTime = 0;

    public static void main(String[] args) {
        QueueTester client1 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "1", 20, true));
        QueueTester client2 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "2", 20, true));
        QueueTester client3 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "3", 20, true));
        QueueTester client4 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "4", 20, true));
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
        while (counter <= 200000) {
            client1.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client1.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client2.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client2.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client3.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client3.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client4.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
            client4.push(new QMessage((counter % 4) + "DODA", counter + "BLAH BLAH" + (counter++ % 4)));
        }
//            }
//        }, 0, 20);
        System.out.println("Published all messages **********************************");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client1.start();
            }
        }, 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client2.start();
            }
        }, 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client3.start();
            }
        }, 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client4.start();
            }
        }, 1);
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
        float divider = timeTaken / 1000;
        int ct = CTR;
        LOGGER.info("Completed {} no of messages in {} millis. Throughput per Sec : {}", ct, timeTaken, ct / divider);
    }

    private static int CTR = 0;
    private static final ReentrantLock LOCK = new ReentrantLock();
}
