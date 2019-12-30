package com.rags.tools.mbq.client;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.server.MBQueueServer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MBQueuePublisher implements QueueClient {

    private final Client client;
    private final MBQueueServer server;
    private Timer timer;

    public MBQueuePublisher(MBQueueServer server, QConfig config) {
        this.server = server;
        this.client = server.registerClient(config);
    }

    @Override
    public void push(QMessage message) {
        server.push(this.client, message);
    }

    @Override
    public void push(List<QMessage> messages) {
        server.push(this.client, messages);
    }

    @Override
    public void start() {
        if (timer != null) {
            throw new MBQException("Client is still running, cant start another instance");
        }
        this.timer = new Timer();

        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                server.ping(client);
            }
        }, 500, 2000);
    }

    @Override
    public void stop() {
        if (timer == null) {
            throw new MBQException("Client is already stopped");
        }

        timer.cancel();
        timer = null;
    }
}
