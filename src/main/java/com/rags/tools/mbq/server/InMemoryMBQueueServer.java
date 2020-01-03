package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.*;
import com.rags.tools.mbq.queue.pending.InMemoryPendingQueueMap;
import com.rags.tools.mbq.queue.pending.PendingQueue;
import com.rags.tools.mbq.queue.pending.PendingQueueMap;
import com.rags.tools.mbq.util.HashingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryMBQueueServer extends AbstractMBQueueServer {

    private static final MBQueue QUEUE = new InMemoryMBQueue();
    private static final PendingQueueMap ALL_PENDING_MESSAGES = new InMemoryPendingQueueMap();

    @Override
    protected MBQueue getQueue() {
        return QUEUE;
    }

    @Override
    protected PendingQueueMap getPendingQueueMap() {
        return ALL_PENDING_MESSAGES;
    }
}
