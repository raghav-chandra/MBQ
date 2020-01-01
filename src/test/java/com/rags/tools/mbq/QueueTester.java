package com.rags.tools.mbq;

import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QueueTester extends MBQueueClient {

    protected QueueTester(QConfig config) {
        super(config);
    }

    public static int counter = 1;

    public static void main(String[] args) {
        QueueTester client1 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "BLAH", 10, true));
        QueueTester client2 = new QueueTester(new QConfig("localhost", 123, "RAGHAV", "CHANDRA", 10, true));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client1.push(new QMessage((counter % 3) + "DODA", counter + "BLAH BLAH" + (counter++ % 3)));
                client2.push(new QMessage((counter % 3) + "DODA", counter + "BLAH BLAH" + (counter++ % 3)));
            }
        }, 100, 100);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client1.start();
            }
        }, 0);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client2.start();
            }
        }, 0);
    }

    @Override
    public void onMessage(List<Object> qItems) {
        qItems.forEach(it -> System.out.println(getClient().getName() + " -> " + it));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
