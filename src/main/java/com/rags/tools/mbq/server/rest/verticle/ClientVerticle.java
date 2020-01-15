package com.rags.tools.mbq.server.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.server.rest.ErrorMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

public class ClientVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();

        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(new QConfig("localhost", 99999, null, null, -1, QueueType.LOCAL_IN_MEMORY));
        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("ClientWorker", 25);

        eventBus.<EventBusRequest>consumer(RequestType.REGISTER_CLIENT.name(), regClientHandler -> {
            Client client = (Client) regClientHandler.body().getReqObj();
            if (client.isInValidForRegistration()) {
                regClientHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    Client clientWithId = mbQueueServer.registerClient(client);
                    workerHandler.complete(clientWithId);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        regClientHandler.reply(resHandler.result());
                    } else {
                        regClientHandler.fail(ErrorMessage.CLIENT_REGISTER_FAILED.getCode(), ErrorMessage.CLIENT_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<EventBusRequest>consumer(RequestType.REGISTER_HEARTBEAT.name(), regClientHandler -> {
            Client client = (Client) regClientHandler.body().getReqObj();
            if (!client.isInValid()) {
                regClientHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    String hearBeatId = mbQueueServer.ping(client);
                    client.setHeartBeatId(hearBeatId);
                    workerHandler.complete(client);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        regClientHandler.reply(resHandler.result());
                    } else {
                        regClientHandler.fail(ErrorMessage.PING_REGISTER_FAILED.getCode(), ErrorMessage.PING_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });
    }
}
