package com.rags.tools.mbq.endpoint.server.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.endpoint.server.ErrorMessage;
import com.rags.tools.mbq.endpoint.server.RequestType;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.server.MBQServerInstance;
import com.rags.tools.mbq.server.MBQueueServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class QueueVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(new QConfig("localhost", 99999, null, null, -1, QueueType.LOCAL_IN_MEMORY));

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("ClientWorker", 25);
        eventBus.<JsonObject>consumer(RequestType.PULL_MESSAGES.name(), regClientHandler -> {
            JsonObject client = regClientHandler.body().getJsonObject(RequestType.REGISTER_CLIENT.name());
            String host = regClientHandler.body().getString("remoteHost");
            boolean validated = ClientVerticle.validateClientRegister(client);
            if (!validated) {
                regClientHandler.fail(ErrorMessage.CODE_8000.getCode(), ErrorMessage.CODE_8000.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = mbQueueServer.pull(new Client(client.getString("id"), client.getString("name"), client.getString("queueName"), client.getInteger("batch")));
                    workerHandler.complete(messages);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        regClientHandler.reply(client);
                    } else {
                        regClientHandler.fail(ErrorMessage.CODE_8001.getCode(), ErrorMessage.CODE_8001.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });
    }

}
