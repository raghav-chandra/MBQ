package com.rags.tools.mbq.server.rest.messagecodec;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Map;

public class CommitRollbackRequest {

    private final Client client;
    private final Map<QueueStatus, List<String>> ids;
    private final Map<String, List<QMessage>> pushMessages;

    public CommitRollbackRequest() {
        this(null, null, null);
    }

    public CommitRollbackRequest(Client client, Map<QueueStatus, List<String>> processedIds, Map<String, List<QMessage>> pushMessages) {
        this.client = client;
        this.ids = processedIds;
        this.pushMessages = pushMessages;
    }

    public Client getClient() {
        return client;
    }

    public Map<QueueStatus, List<String>> getIds() {
        return ids;
    }

    public Map<String, List<QMessage>> getPushMessages() {
        return pushMessages;
    }
}
