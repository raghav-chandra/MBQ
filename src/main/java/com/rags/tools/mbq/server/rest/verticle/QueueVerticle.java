package com.rags.tools.mbq.server.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.server.rest.ErrorMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.server.rest.messagecodec.PushRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import com.rags.tools.mbq.stats.MBQStatsService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class QueueVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        JsonObject config = config();

        QConfig.ServerConfig serverConfig = getServerConfig(config);

        MBQueueServer server = MBQServerInstance.createOrGet(serverConfig);

        registerStatsRequestConsumer(serverConfig.getStatsCollectorClass()); //Service stats calls

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker", 5000);

        eventBus.<EventBusRequest>consumer(RequestType.PULL_MESSAGES.name(), pullHandler -> {
            Client client = (Client) pullHandler.body().getReqObj();
            if (client == null || client.isInValid()) {
                pullHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = server.pull(client);
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pullHandler.reply(resHandler.result());
                    } else {
                        pullHandler.fail(ErrorMessage.MESSAGE_PULL_FAILED.getCode(), ErrorMessage.MESSAGE_PULL_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.PUSH_MESSAGES.name(), pushHandler -> {
            PushRequest req = (PushRequest) pushHandler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                pushHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else if (req.getMessages() == null || req.getMessages().isEmpty()) {
                pushHandler.fail(ErrorMessage.MESSAGES_INVALID.getCode(), ErrorMessage.MESSAGES_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    System.out.println("Push Message Client Name " + req.getClient().getName() + ", Client : " + req.getClient());
                    List<MBQMessage> messages = server.push(req.getClient(), req.getMessages());
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pushHandler.reply(resHandler.result());
                    } else {
                        pushHandler.fail(ErrorMessage.MESSAGE_PUBLISHING_FAILED.getCode(), ErrorMessage.MESSAGE_PUBLISHING_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.REQUEST_COMMIT.name(), pushHandler -> {
            CommitRollbackRequest req = (CommitRollbackRequest) pushHandler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                pushHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.commit(req.getClient(), req.getIds(), req.getPushMessages());
                    workerHandler.complete(isCommit);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pushHandler.reply(resHandler.result());
                    } else {
                        pushHandler.fail(ErrorMessage.MESSAGE_COMMIT_FAILED.getCode(), ErrorMessage.MESSAGE_COMMIT_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.REQUEST_ROLLBACK.name(), pushHandler -> {
            CommitRollbackRequest req = (CommitRollbackRequest) pushHandler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                pushHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else if (req.getIds() == null || req.getIds().isEmpty()) {
                pushHandler.fail(ErrorMessage.MESSAGES_NOT_FOUND_FOR_ROLLBACK.getCode(), ErrorMessage.MESSAGES_NOT_FOUND_FOR_ROLLBACK.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.rollback(req.getClient(), req.getIds());
                    workerHandler.complete(isCommit);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pushHandler.reply(resHandler.result());
                    } else {
                        pushHandler.fail(ErrorMessage.MESSAGE_ROLLBACK_FAILED.getCode(), ErrorMessage.MESSAGE_ROLLBACK_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });
    }

    private void registerStatsRequestConsumer(String collectorClass) {
        MBQStatsService statsService = MBQStatsService.getInstance(collectorClass);
    }

    private QConfig.ServerConfig getServerConfig(JsonObject config) {
        QueueType queueType = QueueType.valueOf(config.getString("queue.type"));
        QConfig.Builder builder = new QConfig.Builder()
                .setQueueType(queueType)
                .setStatsCollectorClass(config.containsKey("stats.collector") ? config.getString("stats.collector") : "com.rags.tools.mbq.stats.collectors.NoOpStatsCollector");
        switch (queueType) {
            case SINGLE_JVM_INMEMORY:
                break;
            case SINGLE_JVM_RDB:
            case SINGLE_JVM_HAZELCAST:
            case SINGLE_JVM_MONGO_DB:
                builder.setUrl(config.getString("url"))
                        .setUser(config.getString("username"))
                        .setPassword(config.getString("password"))
                        .setDbDriver(config.getString("driver"))
                        .setValidationQuery(config.getString("validationQuery"))
                        .setMaxxConn(config.getInteger("maxxConn") == null ? 1 : config.getInteger("maxxConn"));
                break;
            default:
                throw new MBQException("Queue Type is not supported");
        }
        return builder.create().getServerConfig();
    }
}
