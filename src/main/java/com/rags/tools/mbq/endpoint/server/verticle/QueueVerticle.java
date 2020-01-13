package com.rags.tools.mbq.endpoint.server.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.endpoint.server.ErrorMessage;
import com.rags.tools.mbq.endpoint.server.RequestType;
import com.rags.tools.mbq.endpoint.server.messagecodec.DefMessageCodec;
import com.rags.tools.mbq.endpoint.server.messagecodec.PushRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.server.MBQServerInstance;
import com.rags.tools.mbq.server.MBQueueServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class QueueVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();

        MBQueueServer mbQueueServer = MBQServerInstance.createOrGet(new QConfig("localhost", 99999, null, null, -1, QueueType.LOCAL_IN_MEMORY));
        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("ClientWorker", 25);

        eventBus.<JsonObject>consumer(RequestType.PULL_MESSAGES.name(), pullHandler -> {
            JsonObject client = pullHandler.body().getJsonObject(RequestType.REGISTER_CLIENT.name());
            String host = pullHandler.body().getString("remoteHost");
            boolean validated = ClientVerticle.validateClientRegister(client);
            if (!validated) {
                pullHandler.fail(ErrorMessage.CLIENT_INVALID.getCode(), ErrorMessage.CLIENT_INVALID.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = mbQueueServer.pull(new Client(client.getString("id"), client.getString("name"), client.getString("queueName"), client.getInteger("batch")));
                    workerHandler.complete(messages);
                }, resHandler -> {
                    if (resHandler.succeeded()) {
                        pullHandler.reply(client);
                    } else {
                        pullHandler.fail(ErrorMessage.CLIENT_REGISTER_FAILED.getCode(), ErrorMessage.CLIENT_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            }
        });

        eventBus.<JsonObject>consumer(RequestType.PUSH_MESSAGES.name(), pushHandler -> {
            JsonObject pushRequest = pushHandler.body().getJsonObject(RequestType.PUSH_MESSAGES.name());
            ErrorMessage error = validateRequest(pushRequest);
            if (error == null) {
                JsonObject c = pushRequest.getJsonObject("client");
                Client client = new Client(c.getString("id"), c.getString("name"), c.getString("queueName"), c.getInteger("batch"));
                List<QMessage> qMessages = Json.decodeValue(c.getJsonArray("messages").toBuffer(), List.class);
                workers.executeBlocking(workerHandler -> workerHandler.complete(new JsonArray(mbQueueServer.push(client, qMessages))), resHandler -> {
                    if (resHandler.succeeded()) {
                        pushHandler.reply(resHandler.result());
                    } else {
                        pushHandler.fail(ErrorMessage.CLIENT_REGISTER_FAILED.getCode(), ErrorMessage.CLIENT_REGISTER_FAILED.getMessage() + resHandler.cause().getMessage());
                    }
                });
            } else {
                pushHandler.fail(error.getCode(), error.getMessage());
            }

        });
    }

    private ErrorMessage validateRequest(JsonObject pushRequest) {
        JsonObject client = pushRequest.getJsonObject("client");
        JsonArray messages = pushRequest.getJsonArray("messages");
        if (ClientVerticle.validateClientRegister(client)) {
            return ErrorMessage.CLIENT_INVALID;
        }
        if (messages == null || messages.isEmpty()) {
            return ErrorMessage.MESSAGES_INVALID;
        }
        return null;
    }

}
