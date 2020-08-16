package com.rags.tools.mbq.server.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.qserver.MBQServerInstance;
import com.rags.tools.mbq.qserver.MBQueueServer;
import com.rags.tools.mbq.server.rest.ErrorMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class MBQueueDSVerticle extends AbstractVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        JsonObject config = config();

        QConfig.ServerConfig serverConfig = null;//        getServerConfig(config);

        MBQueueServer server = MBQServerInstance.createOrGet(serverConfig);

//        registerStatsRequestConsumer(serverConfig.getStatsCollectorClass()); //Service stats calls

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("QueueWorker");

        /*eventBus.<EventBusRequest>consumer(RequestType.LOOKUP_ITEMS.name(), pullHandler -> {
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
        });*/
    }
}
