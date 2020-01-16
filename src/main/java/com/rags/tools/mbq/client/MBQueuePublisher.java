package com.rags.tools.mbq.client;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MBQueuePublisher implements QueueClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQueuePublisher.class);

    private final Client client;
    private final MBQueueServer server;
    private Timer timer;

    public MBQueuePublisher(QConfig config) {
        this.server = MBQServerInstance.createOrGet(config.getServerConfig());
        this.client = server.registerClient(new Client(config.getClientConfig().getWorkerName(), config.getClientConfig().getPollingQueue(), config.getClientConfig().getBatch()));
    }


    @Override
    public void push(QMessage message) {
        LOGGER.info("Publishing message with Seq Key {} to Queue {}", message.getSeqKey(), getClient().getQueueName());
        server.push(this.client, message);
    }

    @Override
    public void push(List<QMessage> messages) {
        LOGGER.info("Publishing {} messages to Queue", messages.size());
        server.push(this.client, messages);
    }

    @Override
    public void start() {
        LOGGER.info("Starting Queue Message Consumer for Client [{}]", getClient());
        if (timer != null) {
            throw new MBQException("Client is still running, cant start another instance");
        }
        this.timer = new Timer();

        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String hb = server.ping(client);
                client.setHeartBeatId(hb);
            }
        }, 500, 2000);
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down Q Consumer Client ");

        if (timer == null) {
            throw new MBQException("Client is already stopped");
        }

        timer.cancel();
        timer = null;
    }

    protected Client getClient() {
        return client;
    }

    protected MBQueueServer getServer() {
        return server;
    }
}