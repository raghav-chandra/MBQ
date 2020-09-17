package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;
import java.util.Map;

/**
 * @author ragha
 * @since 29-12-2019
 */
public interface MBQDataStore {

    /**
     * Retrieves items from the queue based on the item id
     *
     * @param queueName Queue Name
     * @param id        q item id
     * @return MBQ Message
     */
    MBQMessage get(String queueName, String id);

    /**
     * Retrieves items from the queue based on the item ids
     *
     * @param queueName Queue Name
     * @param ids       q item ids
     * @return MBQ Messages
     */
    List<MBQMessage> get(String queueName, List<String> ids);

    /**
     * Retrieves all Pending Item IDS on Startup
     *
     * @return MBQ pending messages ID and QueueName
     */
    Map<String, List<IdSeqKey>> getAllPendingItems();

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

    /**
     * Updates status of queue messages
     *
     * @param queueName Queue Name
     * @param ids       queue item ids
     * @return true if update is success
     */
    boolean updateStatus(String queueName, Map<QueueStatus, List<String>> ids);

    /**
     * Marks all prev statuc to new Status in the queue
     *
     * @param prevStatus prev status in the queue
     * @param newStatus  new status in the queue
     */
    void updateStatus(QueueStatus prevStatus, QueueStatus newStatus);

    /**
     * Searches Items from the queue data store.
     *
     * @param searchRequest request object
     */
    List<MBQMessage> search(SearchRequest searchRequest);
}
