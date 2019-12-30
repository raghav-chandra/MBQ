package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;

public class InMemoryMBQueueServer implements MBQueueServer {
    @Override
    public Client registerClient(QConfig config) {
        return null;
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        return null;
    }

    @Override
    public boolean commit(Client client, List<String> ids) {
        return false;
    }

    @Override
    public boolean rollback(Client client, List<String> ids) {
        return false;
    }

    @Override
    public List<MBQMessage> push(Client client, List<QMessage> messages) {
        return null;
    }

    @Override
    public MBQMessage push(Client client, QMessage message) {
        return null;
    }

    @Override
    public void ping(Client client) {

    }
}
