package com.rags.tools.mbq.endpoint.server.verticle;

import com.hazelcast.core.HazelcastInstance;
import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.endpoint.server.ErrorMessage;
import com.rags.tools.mbq.endpoint.server.RequestType;
import com.rags.tools.mbq.endpoint.server.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.endpoint.server.messagecodec.EventBusRequest;
import com.rags.tools.mbq.endpoint.server.messagecodec.PushRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.server.MBQServerInstance;
import com.rags.tools.mbq.server.MBQueueServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import java.util.List;

public class QueueVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();

        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(new QConfig("localhost", 99999, null, null, -1, QueueType.LOCAL_IN_MEMORY));
        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker", 100);

        eventBus.<EventBusRequest>consumer(RequestType.PULL_MESSAGES.name(), pullHandler -> {
            Client client = (Client) pullHandler.body().getReqObj();
            if (client.isInValid()) {
                pullHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = mbQueueServer.pull(client);
                    workerHandler.complete(messages);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pullHandler.reply(resHandler.result());
                    } else {
                        pullHandler.fail(ErrorMessage.CLIENT_REGISTER_FAILED.getCode(), ErrorMessage.CLIENT_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
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
                    List<MBQMessage> messages = mbQueueServer.push(req.getClient(), req.getMessages());
                    workerHandler.complete(messages);
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
            } else if (req.getIds() == null || req.getIds().isEmpty()) {
                pushHandler.fail(ErrorMessage.MESSAGES_NOT_FOUND_FOR_COMMIT.getCode(), ErrorMessage.MESSAGES_NOT_FOUND_FOR_COMMIT.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    boolean isCommit = mbQueueServer.commit(req.getClient(), req.getIds());
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
                    boolean isCommit = mbQueueServer.rollback(req.getClient(), req.getIds());
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
}
