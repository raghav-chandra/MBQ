package com.rags.tools.mbq.server.rest.messagecodec;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;

public class CommitRollbackRequest {

    private final Client client;
    private final List<String> ids;
    private final List<QMessage> pushMessages;

    public CommitRollbackRequest() {
        this(null, null, null);
    }

    public CommitRollbackRequest(Client client, List<String> processedIds, List<QMessage> pushMessages) {
        this.client = client;
        this.ids = processedIds;
        this.pushMessages = pushMessages;
    }

    public Client getClient() {
        return client;
    }

    public List<String> getIds() {
        return ids;
    }

    public List<QMessage> getPushMessages() {
        return pushMessages;
    }
}
