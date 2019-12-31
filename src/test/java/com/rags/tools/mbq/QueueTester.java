package com.rags.tools.mbq;

import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.queue.InMemoryMBQueue;

public class QueueTester extends MBQueueClient {

    public static void main(String[] args) {
        System.out.println(new InMemoryMBQueue());
    }

}
