package com.rags.tools.mbq.endpoint.server.messagecodec;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;

public class PushRequest {
    private final Client client;
    private final List<QMessage> messages;

    public PushRequest() {
        this(null, null);
    }

    public PushRequest(Client client, List<QMessage> messages) {
        this.client = client;
        this.messages = messages;
    }

    public Client getClient() {
        return client;
    }

    public List<QMessage> getMessages() {
        return messages;
    }
}
