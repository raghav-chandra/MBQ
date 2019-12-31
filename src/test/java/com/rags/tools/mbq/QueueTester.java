package com.rags.tools.mbq;

import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.InMemoryMBQueue;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QueueTester extends MBQueueClient {

    protected QueueTester(QConfig config) {
        super(config);
    }

    public static int counter = 1;
    public static void main(String[] args) {
        QueueTester client = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "BLAH", 10, true));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.push(new QMessage((counter++ %3) + "DODA", counter + "BLOND"));
            }
        }, 100, 400);

        client.start();
    }

    @Override
    public void onMessage(List<Object> qItems) {
        qItems.forEach(System.out::println);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
