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

    private final MBQueueServer server;
    private final QConfig config;

    private Client client;
    private Timer timer;

    private final Map<String, List<QMessage>> messagesToPushed = new HashMap<>();
    private final List<MBQMessage> processingMessages = new LinkedList<>();

    private final Transaction transaction;

    public MBQueuePublisher(QConfig config) {
        this.server = MBQServerInstance.createOrGet(config.getServerConfig());
        this.config = config;
        this.transaction = new Transaction();
    }

    @Override
    public void push(QMessage message) {
        push(Collections.singletonList(message));
    }

    @Override
    public void push(List<QMessage> messages) {
        pushMessages(messages, client.getQueueName());
    }

    private void pushMessages(List<QMessage> messages, String queueName) {
        validateClient();
        if (transaction.getStatus() != QTransStatus.START) {
            throw new MBQException("Message can not be pushed without transaction");
        }
        LOGGER.info("Publishing {} messages to Queue", messages.size());
        if (!messagesToPushed.containsKey(queueName)) {
            messagesToPushed.put(queueName, new LinkedList<>());
        }
        messagesToPushed.get(queueName).addAll(messages);
    }


    @Override
    public void push(QMessage message, String queueName) {
        pushMessages(Collections.singletonList(message), client.getQueueName());
    }

    @Override
    public void push(List<QMessage> messages, String queueName) {
        pushMessages(messages, client.getQueueName());
    }

    @Override
    public void start() {
        LOGGER.info("Starting Queue Message Consumer for Client [{}]", getClient());
        if (timer != null) {
            throw new MBQException("Client is still running, cant start again");
        }
        this.timer = new Timer(true);
        this.client = server.registerClient(new Client(config.getClientConfig().getWorkerName(), config.getClientConfig().getPollingQueue(), config.getClientConfig().getBatch()));
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                client.setHeartBeatId(server.ping(client));
            }
        }, 0, 1000);
    }

    @Override
    public void stop() {
        validateClient();

        LOGGER.info("Shutting down Q Consumer Client ");

        if (timer == null) {
            throw new MBQException("Client is already stopped");
        }

        timer.cancel();
        this.client = null;
        timer = null;
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    private void rollBackQueueTrans() {
        validateClient();
        LOGGER.info("Rolling back for the processed items of client [{}]", getClient());

        boolean success = getServer().rollback(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                .collect(Collectors.toList()));
        if (success) {
            processingMessages.clear();
            messagesToPushed.clear();
        } else {
            throw new MBQException("Failed while rolling back transaction");
        }
    }

    private void validateClient() {
        if (this.client == null || this.client.getId() == null || this.client.getHeartBeatId() == null) {
            throw new MBQException("Client is not registered. Please call start() on client");
        }
    }

    private void commitQueueTrans() {
        validateClient();
        if (!processingMessages.isEmpty() || !messagesToPushed.isEmpty()) {
            LOGGER.info("Committing Transaction for client [{}] with processedItems [{}] and message pushed to queues [{}]"
                    , getClient(), processingMessages.size(), messagesToPushed.size());
            boolean success = getServer().commit(getClient(), processingMessages.parallelStream().map(MBQMessage::getId)
                    .collect(Collectors.toList()), messagesToPushed);
            if (success) {
                processingMessages.clear();
                messagesToPushed.clear();
            } else {
                throw new MBQException("Failed while commiting transaction");
            }
        }
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

        private Transaction() {

        }

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