package com.rags.tools.mbq.endpoint.server.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.endpoint.server.ErrorMessage;
import com.rags.tools.mbq.endpoint.server.RequestType;
import com.rags.tools.mbq.endpoint.server.messagecodec.DefMessageCodec;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.server.MBQServerInstance;
import com.rags.tools.mbq.server.MBQueueServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class ClientVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        eventBus.registerCodec(new DefMessageCodec<Client>());

        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(new QConfig("localhost", 99999, null, null, -1, QueueType.LOCAL_IN_MEMORY));
        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("ClientWorker", 25);

        eventBus.<JsonObject>consumer(RequestType.REGISTER_CLIENT.name(), regClientHandler -> {
            JsonObject client = regClientHandler.body().getJsonObject(RequestType.REGISTER_CLIENT.name());
            String host = regClientHandler.body().getString("remoteHost");
            boolean validated = validateClientFields(client);
            if (!validated) {
                regClientHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    Client clientWithId = mbQueueServer.registerClient(new Client(null, client.getString("name"), client.getString("queueName"), client.getInteger("batch")));
                    workerHandler.complete(client.put("id", clientWithId.getId()));
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        regClientHandler.reply(client);
                    } else {
                        regClientHandler.fail(ErrorMessage.CLIENT_REGISTER_FAILED.getCode(), ErrorMessage.CLIENT_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<JsonObject>consumer(RequestType.REGISTER_HEARTBEAT.name(), regClientHandler -> {
            JsonObject client = regClientHandler.body().getJsonObject(RequestType.REGISTER_HEARTBEAT.name());
            String host = regClientHandler.body().getString("remoteHost");
            boolean validated = ClientVerticle.validateClientRegister(client);
            if (!validated) {
                regClientHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    String hearBeatId = mbQueueServer.ping(new Client(null, client.getString("name"), client.getString("queueName"), client.getInteger("batch")));
                    workerHandler.complete(hearBeatId);
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

    private static boolean validateClientFields(JsonObject client) {
        return client.containsKey("name")
                && client.containsKey("queueName")
                && client.containsKey("batch");
    }

    public static boolean validateClientRegister(JsonObject clientObj) {
        return clientObj!=null && clientObj.containsKey("id") && validateClientFields(clientObj);
    }
}
