package com.rags.tools.mbq.connection.rest.verticle;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.connection.rest.ErrorMessage;
import com.rags.tools.mbq.connection.rest.RequestType;
import com.rags.tools.mbq.connection.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.PushRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.UpdateStatusRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.qserver.MBQueueServer;
import com.rags.tools.mbq.qserver.QueueServer;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class QueueVerticle extends CommonVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();

        QueueServer server = MBQueueServer.getInstance(getServerConfig(config()));

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker", 5000);

        eventBus.<EventBusRequest<Client>>consumer(RequestType.PULL_MESSAGES.name(), handler -> {
            Client client = handler.body().getReqObj().setHost(handler.body().getRemoteHost());
            if (client == null || client.isInValid()) {
                fail(handler, ErrorMessage.CLIENT_INVALID);
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
                fail(handler, ErrorMessage.CLIENT_INVALID);
            } else if (req.getMessages() == null || req.getMessages().isEmpty()) {
                fail(handler, ErrorMessage.MESSAGES_INVALID);
            } else {
                req.getClient().setHost(handler.body().getRemoteHost());
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = server.push(req.getClient().setHost(handler.body().getRemoteHost()), req.getMessages());
                    workerHandler.complete(new JsonArray(messages.parallelStream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler(handler, ErrorMessage.MESSAGE_PUBLISHING_FAILED));
            }
        });

        eventBus.<EventBusRequest<CommitRollbackRequest>>consumer(RequestType.REQUEST_COMMIT.name(), handler -> {
            CommitRollbackRequest req = handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                fail(handler, ErrorMessage.CLIENT_INVALID);
            } else {
                req.getClient().setHost(handler.body().getRemoteHost());
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.commit(req.getClient().setHost(handler.body().getRemoteHost()), req.getIds(), req.getPushMessages());
                    workerHandler.complete(isCommit);
                }, resHandler(handler, ErrorMessage.MESSAGE_COMMIT_FAILED));
            }
        });

        eventBus.<EventBusRequest<CommitRollbackRequest>>consumer(RequestType.REQUEST_ROLLBACK.name(), handler -> {
            CommitRollbackRequest req = handler.body().getReqObj();
            if (req.getClient() == null || req.getClient().isInValid()) {
                fail(handler, ErrorMessage.CLIENT_INVALID);
            } else if (req.getIds() == null || req.getIds().isEmpty()) {
                fail(handler, ErrorMessage.MESSAGES_NOT_FOUND_FOR_ROLLBACK);
            } else {
                req.getClient().setHost(handler.body().getRemoteHost());
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = server.rollback(req.getClient().setHost(handler.body().getRemoteHost()), req.getIds());
                    workerHandler.complete(isCommit);
                }, resHandler(handler, ErrorMessage.MESSAGE_ROLLBACK_FAILED));
            }
        });

        eventBus.<EventBusRequest<UpdateStatusRequest>>consumer(RequestType.UPDATE_STATUS.name(), handler -> {
            UpdateStatusRequest req = handler.body().getReqObj();
            if (req.getIds() == null || req.getIds().isEmpty() || req.getStatus() == null) {
                fail(handler, ErrorMessage.MESSAGES_NOT_FOUND_FOR_UPDATE);
            } else {
                workers.executeBlocking(workerHandler -> {
                    workerHandler.complete(server.update(req.getIds(), req.getStatus()));
                }, resHandler(handler, ErrorMessage.MESSAGE_ROLLBACK_FAILED));
            }
        });
    }

    private void fail(Message handler, ErrorMessage error) {
        handler.fail(error.getCode(), error.getMessage());
    }
}
