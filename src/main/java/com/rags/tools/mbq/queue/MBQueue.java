package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;

import java.util.List;

/**
 * @author ragha
 * @since 29-12-2019
 */
public interface MBQueue {

    /**
     * Pulls messages for processing
     *
     * @param client client information
     * @return LIst of messages to be processed
     */
    List<MBQMessage> pull(Client client);

    /**
     * Pushes messages to the queue
     *
     * @param client   client information
     * @param messages messages to be pushed
     * @return list of messages that has been pushed
     */
    List<MongoMBQueue> push(Client client, List<MBQueue> messages);

    /**
     * Updates status of queue messages
     *
     * @param client client information
     * @param ids    queue item ids
     * @param status status to be updated
     * @return true if update is success
     */
    boolean updateStatus(Client client, List<String> ids, QueueStatus status);
}
