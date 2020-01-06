package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Map;

/**
 * @author ragha
 * @since 29-12-2019
 */
public interface MBQueue {

    /**
     * Retrieves items from the queue based on the item id
     *
     * @param queueName Queue Name
     * @param id        q item id
     * @return MBQ Message
     */
    MBQMessage get(String queueName, String id);

    /**
     * Retrieves items from the queue based on the SeqKey and Status
     *
     * @param queueName Queue Name
     * @param seqKey    sequence key
     * @param status    Item Status
     * @return MBQ Message
     */
    List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status);

    /**
     * Retrieves all Pending Item IDS on Startup
     *
     * @return MBQ pending messages ID and QueueName
     */
    Map<String, List<String>> getAllPendingIds();

    /**
     * Pulls messages for processing
     *
     * @param queueName Queue name
     * @param ids       message Ids
     * @return LIst of messages to be processed
     */
    List<MBQMessage> pull(String queueName, List<String> ids);

    /**
     * Pushes messages to the queue
     *
     * @param queueName queue name
     * @param messages  messages to be pushed
     * @return list of messages that has been pushed
     */
    List<MBQMessage> push(String queueName, List<QMessage> messages);

    /**
     * Updates status of queue messages
     *
     * @param queueName Queue Name
     * @param ids       queue item ids
     * @param status    status to be updated
     * @return true if update is success
     */
    boolean updateStatus(String queueName, List<String> ids, QueueStatus status);
}
