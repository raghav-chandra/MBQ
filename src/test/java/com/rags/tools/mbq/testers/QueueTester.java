package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.client.MBQueuePublisher;
import com.rags.tools.mbq.message.MBQMessage;
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

    private static final int DIFF_SEQ = 500;

    private static final int noOfQueues = 1;

    static void execute(int publishers, int consumers, QConfig.Builder config) {
        List<MBQueuePublisher> allPublishers = new ArrayList<>();
        int queues = Math.min(noOfQueues, consumers);
        for (int i = 0; i < publishers; i++) {
            allPublishers.add(new MBQueuePublisher(config.clone().setWorkerName("publisher-" + i).setPollingQueue(config.create().getClientConfig().getPollingQueue() + (i % queues)).create()));
        }
        allPublishers.forEach(MBQueuePublisher::start);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long l = System.currentTimeMillis();
                allPublishers.parallelStream().forEach(client -> {
                    List<QMessage> messages = new LinkedList<>();
                    for (int i = 0; i < 10; i++) {
                        messages.add(new QMessage((counter % DIFF_SEQ) + "DODA", (counter + "BLAH BLAH" + (counter++ % DIFF_SEQ)).getBytes()));
                    }
                    Transaction transaction = client.getTransaction();
                    transaction.start();
                    client.push(messages);
                    transaction.commit();
                });
            }
        }, 100, 200);

        List<QueueTester> allConsumers = new ArrayList<>();
        for (int i = 0; i < consumers; i++) {
            allConsumers.add(new QueueTester(config.clone().setWorkerName("consumer-" + i).setPollingQueue(config.create().getClientConfig().getPollingQueue() + (i % queues)).create()));
        }

        allConsumers.forEach(client -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.start();
            }
        }, 1));
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onMessage(List<MBQMessage.ProcessingItem> qItems) {
        qItems.forEach(it -> {
            LOCK.lock();
            CTR++;
            LOCK.unlock();
        });
        int ct = CTR;

        long timeTaken = System.currentTimeMillis() - startTime;
        float divider = timeTaken / 1000f;
        LOGGER.info("Completed {} no of messages in {} millis. Throughput per Sec : {}", ct, timeTaken, ct / divider);

       /* if(qItems.size()>1) {
            qItems.get(1).markError();
            for (int i = 2; i < qItems.size(); i++) {
                qItems.get(i).markPending();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private static int CTR = 0;
    private static final ReentrantLock LOCK = new ReentrantLock();
}
