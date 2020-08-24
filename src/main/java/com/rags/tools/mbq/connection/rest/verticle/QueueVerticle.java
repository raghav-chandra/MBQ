package com.rags.tools.mbq.connection.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.QueueServer;
import com.rags.tools.mbq.connection.rest.ErrorMessage;
import com.rags.tools.mbq.connection.rest.RequestType;
import com.rags.tools.mbq.connection.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.PushRequest;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class QueueVerticle extends CommonVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        JsonObject config = config();

        QConfig.ServerConfig serverConfig = getServerConfig(config);
        QueueServer server = MBQServerInstance.createOrGet(serverConfig);

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker", 5000);

        eventBus.<EventBusRequest<Client>>consumer(RequestType.PULL_MESSAGES.name(), handler -> {
            Client client = handler.body().getReqObj().setHost(handler.body().getRemoteHost());
            if (client == null || client.isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = server.pull(client);
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler(handler, ErrorMessage.MESSAGE_PULL_FAILED));
            }
        });

        eventBus.<EventBusRequest<PushRequest>>consumer(RequestType.PUSH_MESSAGES.name(), handler -> {
            PushRequest req = handler.body().getReqObj();
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

        eventBus.<EventBusRequest<CommitRollbackRequest>>consumer(RequestType.REQUEST_COMMIT.name(), handler -> {
            CommitRollbackRequest req = handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.commit(req.getClient().setHost(handler.body().getRemoteHost()), req.getIds(), req.getPushMessages());
                    workerHandler.complete(isCommit);
                }, resHandler(handler, ErrorMessage.MESSAGE_COMMIT_FAILED));
            }
        });

        eventBus.<EventBusRequest<CommitRollbackRequest>>consumer(RequestType.REQUEST_ROLLBACK.name(), handler -> {
            CommitRollbackRequest req = handler.body().getReqObj();
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
}
