package com.rags.tools.mbq.client;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.message.MBQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public abstract class MBQueueClient extends MBQueuePublisher implements QueueClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MBQueueClient.class);

    private boolean polling = true;

    protected MBQueueClient(QConfig config) {
        super(config);
    }

    @Override
    public void start() {
        super.start();
        new Timer(getConfig().getClientConfig().isDaemon()).schedule(new TimerTask() {
            @Override
            public void run() {
                while (isPolling()) {
                    Transaction transaction = getTransaction();
                    List<MBQMessage> items = getServer().pull(getClient());
                    LOGGER.debug("Polled {} no of messोges to processed for client {}", items.size(), getClient());
                    List<MBQMessage.ProcessingItem> processingItems = items.stream().map(MBQMessage::getProcessingItem).collect(Collectors.toList());
                    getProcessingItems().addAll(processingItems);
                    try {
                        if (!getProcessingItems().isEmpty()) {
                            transaction.start();
                            onMessage(Collections.unmodifiableList(processingItems));
                            transaction.commit();
                        }
                    } catch (Throwable t) {
                        LOGGER.error("Exception occurred while processing items ", t);
                        transaction.rollback();
                    }
                }
            }
        }, 0);
    }

    private boolean isPolling() {
        return polling;
    }

    public abstract void onMessage(List<MBQMessage.ProcessingItem> qItems);

    @Override
    public void stop() {
        super.stop();
        this.polling = false;
    }
}