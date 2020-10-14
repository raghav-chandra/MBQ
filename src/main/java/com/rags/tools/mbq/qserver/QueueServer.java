package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Map;

/**
 * @author ragha
 * @since 29-12-2019
 */
public interface QueueServer {

    /**
     * Registers client with the queue for any communication
     *
     * @param client client configuration
     * @return Client information
     */
    Client registerClient(Client client);

    /**
     * Pulls messages from queue for processing
     *
     * @param client client information
     * @return List of MBQ Messages
     */
    List<MBQMessage> pull(Client client);

    /**
     * Commits queue transactions
     *
     * @param client         client information
     * @param processedIds   item IDs
     * @param messagesToPush all the messages that has to be pushed
     * @return true if commit was successful
     */
    boolean commit(Client client, Map<QueueStatus, List<String>> processedIds, Map<String, List<QMessage>> messagesToPush);

    /**
     * Roll back queue transactions
     *
     * @param client client information
     * @param ids    item IDs
     * @return true if rollback was successful
     */
    boolean rollback(Client client, Map<QueueStatus, List<String>> ids);

    /**
     * Pushes messages to the queue
     *
     * @param client   Client information
     * @param messages messages to be pushed
     * @return messages pushed to the queue
     */
    List<MBQMessage> push(Client client, List<QMessage> messages);

    /**
     * Pushes message to the queue
     *
     * @param client  Client information
     * @param message message to be pushed
     * @return message pushed to the queue
     */
    MBQMessage push(Client client, QMessage message);

    /**
     * Send heartbeat to the Q Server
     *
     * @param client client information
     * @return unique identifier
     */
    String ping(Client client);

    /**
     * Updates Status of Items in the queue
     *
     * @param ids       Item ids
     * @param newStatus new status
     * @return success
     */
    boolean update(List<String> ids, QueueStatus newStatus);
}
