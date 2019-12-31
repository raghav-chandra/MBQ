package com.rags.tools.mbq.client;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.server.InMemoryMBQueueServer;
import com.rags.tools.mbq.server.MBQueueServer;
import com.rags.tools.mbq.server.MBQueueServerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public abstract class MBQueueClient extends MBQueuePublisher implements QueueClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MBQueueClient.class);

    private boolean poll = true;
    private final List<MBQMessage> processingMessages = new ArrayList<>();
    private QTransStatus status = QTransStatus.INIT;

    protected MBQueueClient(QConfig config) {
        super(config);
    }

    @Override
    public void start() {
        super.start();
        while (this.poll) {
            LOGGER.info("Polling queue to process new messages");
            List<MBQMessage> items = getServer().pull(getClient());
            LOGGER.info("Found {} no of messोges to processed", items.size());
            processingMessages.addAll(items);
            try {
                if (!processingMessages.isEmpty()) {
                    this.status = QTransStatus.INIT;
                    onMessage(items.stream().map(QMessage::getMessage).collect(Collectors.toUnmodifiableList()));
                    commitQueueTrans();
                }
            } catch (Throwable t) {
                LOGGER.error("Exception occurred while processing items ", t);
                rollBackQueueTrans();
            }
        }
    }

    private void commitQueueTrans() {
        LOGGER.info("Commiting Transaction for the processed items");
        if (processingMessages.isEmpty()) {
            throw new MBQException("There's no item processed that has to be commited");
        }

        if (this.status != QTransStatus.INIT && this.status != QTransStatus.OPEN) {
            throw new MBQException("Q Transaction is not started that has to be commited");
        }

        boolean sucess = getServer().commit(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                .collect(Collectors.toList()));
        if (sucess) {
            processingMessages.clear();
            this.status = QTransStatus.COMMITED;
        } else {
            throw new MBQException("Failed while commiting transaction");
        }
    }

    private void rollBackQueueTrans() {
        LOGGER.info("Rolling Back Transaction");
        if (processingMessages.isEmpty()) {
            throw new MBQException("There's no item processed that has to be rolled back");
        }

        if (this.status != QTransStatus.INIT && this.status != QTransStatus.OPEN) {
            throw new MBQException("Q Transaction is not started that has to be rolled back");
        }

        boolean sucess = getServer().rollback(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                .collect(Collectors.toList()));
        if (sucess) {
            processingMessages.clear();
            this.status = QTransStatus.ROLLED_BACK;
        } else {
            throw new MBQException("Failed while rolling back transaction");
        }
    }

    public abstract void onMessage(List<Object> qItems);

    @Override
    public void stop() {
        super.stop();
        this.poll = false;
    }

    private enum QTransStatus {
        INIT, OPEN, COMMITED, ROLLED_BACK
    }
}
