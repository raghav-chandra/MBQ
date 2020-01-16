package com.rags.tools.mbq.testers;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.MBQueueClient;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.QueueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DBQueueTester extends QueueTester {
    protected DBQueueTester(QConfig config) {
        super(config);
    }

    public static void main(String[] args) {
        execute(10,0,  QueueType.LOCAL_RDB);
    }
}

