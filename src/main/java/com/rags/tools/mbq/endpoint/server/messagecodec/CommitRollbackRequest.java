package com.rags.tools.mbq.endpoint.server.messagecodec;

import com.rags.tools.mbq.client.Client;

import java.util.List;

public class CommitRollbackRequest {

    private final Client client;
    private final List<String> ids;

    public CommitRollbackRequest(Client client, List<String> ids) {
        this.client = client;
        this.ids = ids;
    }

    public Client getClient() {
        return client;
    }

    public List<String> getIds() {
        return ids;
    }
}
