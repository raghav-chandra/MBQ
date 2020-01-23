package com.rags.tools.mbq.client;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MBQueuePublisher implements QueueClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQueuePublisher.class);

    private final Client client;
    private final MBQueueServer server;
    private Timer timer;

    private final List<QMessage> messagesToPushed = new LinkedList<>();
    private final List<MBQMessage> processingMessages = new LinkedList<>();

    private Transaction transaction;

    public MBQueuePublisher(QConfig config) {
        this.server = MBQServerInstance.createOrGet(config.getServerConfig());
        this.client = server.registerClient(new Client(config.getClientConfig().getWorkerName(), config.getClientConfig().getPollingQueue(), config.getClientConfig().getBatch()));
        this.transaction = new Transaction();
    }

    @Override
    public void push(QMessage message) {
        if (transaction.getStatus() != QTransStatus.START) {
            throw new MBQException("Transaction is not started");
        }

        LOGGER.info("Publishing message with Seq Key {} to Queue {}", message.getSeqKey(), getClient().getQueueName());
        messagesToPushed.add(message);
//        server.push(this.client, message);
    }

    @Override
    public void push(List<QMessage> messages) {
        if (transaction.getStatus() != QTransStatus.START) {
            throw new MBQException("Message can not be pushed without transaction");
        }
        LOGGER.info("Publishing {} messages to Queue", messages.size());
        messagesToPushed.addAll(messages);
//        server.push(this.client, messages);
    }

    @Override
    public void start() {
        LOGGER.info("Starting Queue Message Consumer for Client [{}]", getClient());
        if (timer != null) {
            throw new MBQException("Client is still running, cant start again");
        }
        this.timer = new Timer(true);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                client.setHeartBeatId(server.ping(client));
            }
        }, 0, 500);
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

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    protected void rollBackQueueTrans() {
        LOGGER.info("Rolling back for the processed items of client [{}]", getClient());
        /*if (processingMessages.isEmpty() || messagesToPushed.isEmpty()) {
            throw new MBQException("There's no item processed that has to be rolled back");
        }
*/
        boolean success = getServer().rollback(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                .collect(Collectors.toList()));
        if (success) {
            processingMessages.clear();
            messagesToPushed.clear();
        } else {
            throw new MBQException("Failed while rolling back transaction");
        }
    }

    void commitQueueTrans() {
        LOGGER.info("Committing Transaction for the processed items of client [{}]", getClient());
        /*if (processingMessages.isEmpty() || messagesToPushed.isEmpty()) {
            throw new MBQException("There's no item processed that has to be commited");
        }*/

        boolean success = getServer().commit(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                .collect(Collectors.toList()), getMessagesToPushed());
        if (success) {
            processingMessages.clear();
            messagesToPushed.clear();
        } else {
            throw new MBQException("Failed while commiting transaction");
        }
    }


    protected List<QMessage> getMessagesToPushed() {
        return messagesToPushed;
    }


    protected List<MBQMessage> getProcessingMessages() {
        return processingMessages;
    }

    protected Client getClient() {
        return client;
    }

    protected MBQueueServer getServer() {
        return server;
    }

    public class Transaction {
        private QTransStatus status = QTransStatus.INIT;

        public void start() {
            if (this.status != QTransStatus.ROLLED_BACK && this.status != QTransStatus.COMMITED && this.status != QTransStatus.INIT) {
                throw new MBQException("Another transaction can not be started.");
            }
            this.status = QTransStatus.START;
        }

        public void rollback() {
            if (this.status != QTransStatus.START) {
                throw new MBQException("Q Transaction is not started that has to be rolled back");
            }

            rollBackQueueTrans();
            this.status = QTransStatus.ROLLED_BACK;
        }

        public void commit() {
            if (this.status != QTransStatus.START) {
                throw new MBQException("Q Transaction is not started that has to be commited");
            }

            commitQueueTrans();
            this.status = QTransStatus.COMMITED;
        }

        public QTransStatus getStatus() {
            return this.status;
        }
    }

    private enum QTransStatus {
        INIT, START, COMMITED, ROLLED_BACK;
    }
}