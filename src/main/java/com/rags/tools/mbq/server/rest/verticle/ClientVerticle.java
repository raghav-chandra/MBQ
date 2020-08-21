package com.rags.tools.mbq.server.rest.verticle;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import com.rags.tools.mbq.server.rest.ErrorMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class ClientVerticle extends CommonVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();

        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(getServerConfig(config()));

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("ClientWorker", 100);

        eventBus.<EventBusRequest<Client>>consumer(RequestType.REGISTER_CLIENT.name(), handler -> {
            Client client = handler.body().getReqObj().setHost(handler.body().getRemoteHost());
            if (client.isInValidForRegistration()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(wHandler -> {
                    Client c = mbQueueServer.registerClient(client);
                    wHandler.complete(JsonObject.mapFrom(c));
                }, resHandler(handler, ErrorMessage.CLIENT_REGISTER_FAILED));
            }
        });

        eventBus.<EventBusRequest<Client>>consumer(RequestType.REGISTER_HEARTBEAT.name(), handler -> {
            Client client = handler.body().getReqObj().setHost(handler.body().getRemoteHost());
            if (client == null || client.isInValid()) {
                handler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> workerHandler.complete(mbQueueServer.ping(client))
                        , resHandler(handler, ErrorMessage.PING_REGISTER_FAILED));
            }
        });
    }
}
