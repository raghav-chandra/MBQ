package com.rags.tools.mbq.client;

import com.rags.tools.mbq.message.QMessage;

import java.util.List;

/**
 * @author ragha
 * @since 29-12-2019
 */
public interface QueueClient {

    /**
     * Pushes message to the queue
     *
     * @param message message to be pushed to the queue
     */
    void push(QMessage message);

    /**
     * Pushes messages to the default queue
     *
     * @param messages messages to be pushed to the queue
     */
    void push(List<QMessage> messages);

    /**
     * Pushes message to the specific queue
     *
     * @param message   message to be pushed to the queue
     * @param queueName specified queue name
     */
    void push(QMessage message, String queueName);

    /**
     * Pushes messages to the  specific queue
     *
     * @param messages  messages to be pushed to the queue
     * @param queueName specified queue name
     */
    void push(List<QMessage> messages, String queueName);

    /**
     * Starts Q client
     */
    void start();

    /**
     * Stops Q client
     */
    void stop();

    /**
     * Creates or returns existing Transactions
     *
     * @return Q Transaction
     */
    MBQueuePublisher.Transaction getTransaction();
}
