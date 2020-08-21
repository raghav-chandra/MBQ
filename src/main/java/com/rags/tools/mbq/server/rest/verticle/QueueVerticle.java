package com.rags.tools.mbq.server.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.server.rest.ErrorMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.server.rest.messagecodec.PushRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
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

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker", 5000);

        eventBus.<EventBusRequest>consumer(RequestType.PULL_MESSAGES.name(), handler -> {
            Client client = ((Client) handler.body().getReqObj()).setHost(handler.body().getRemoteHost());
            if (client == null || client.isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = server.pull(client);
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler(handler, ErrorMessage.MESSAGE_PULL_FAILED));
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.PUSH_MESSAGES.name(), handler -> {
            PushRequest req = (PushRequest) handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else if (req.getMessages() == null || req.getMessages().isEmpty()) {
                handler.fail(ErrorMessage.MESSAGES_INVALID.getCode(), ErrorMessage.MESSAGES_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = server.push(req.getClient().setHost(handler.body().getRemoteHost()), req.getMessages());
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler(handler, ErrorMessage.MESSAGE_PUBLISHING_FAILED));
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.REQUEST_COMMIT.name(), handler -> {
            CommitRollbackRequest req = (CommitRollbackRequest) handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.commit(req.getClient().setHost(handler.body().getRemoteHost()), req.getIds(), req.getPushMessages());
                    workerHandler.complete(isCommit);
                }, resHandler(handler, ErrorMessage.MESSAGE_COMMIT_FAILED));
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.REQUEST_ROLLBACK.name(), handler -> {
            CommitRollbackRequest req = (CommitRollbackRequest) handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else if (req.getIds() == null || req.getIds().isEmpty()) {
                handler.fail(ErrorMessage.MESSAGES_NOT_FOUND_FOR_ROLLBACK.getCode(), ErrorMessage.MESSAGES_NOT_FOUND_FOR_ROLLBACK.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.rollback(req.getClient().setHost(handler.body().getRemoteHost()), req.getIds());
                    workerHandler.complete(isCommit);
                }, resHandler(handler, ErrorMessage.MESSAGE_ROLLBACK_FAILED));
            }
        });
    }

    private Handler<AsyncResult<Object>> resHandler(Message<EventBusRequest> handler, ErrorMessage messagePullFailed) {
        return resHandler -> {
            if (resHandler.succeeded()) {
                handler.reply(resHandler.result());
            } else {
                handler.fail(messagePullFailed.getCode(), messagePullFailed.getMessage() + resHandler.cause().getMessage());
            }
        };
    }

    private QConfig.ServerConfig getServerConfig(JsonObject config) {
        QueueType queueType = QueueType.valueOf(config.getString("queue.type"));
        QConfig.Builder builder = new QConfig.Builder()
                .setQueueType(queueType)
                .setStatsCollectorClass(config.getString("stats.collector", "com.rags.tools.mbq.stats.collectors.NoOpStatsCollector"));
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
                        .setMaxxConn(Math.max(config.getInteger("maxxConn", 1), 1));
                break;
            default:
                throw new MBQException("Queue Type is not supported");
        }
        return builder.create().getServerConfig();
    }
}
