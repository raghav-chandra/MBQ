package com.rags.tools.mbq.server.rest;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.server.rest.handler.ClientHandler;
import com.rags.tools.mbq.server.rest.handler.QueueHandler;
import com.rags.tools.mbq.server.rest.messagecodec.DefMessageCodec;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.server.rest.verticle.ClientVerticle;
import com.rags.tools.mbq.server.rest.verticle.QueueVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class MBQServerStartup extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQServerStartup.class);
    private static final List<Class<? extends AbstractVerticle>> VERTICLES = Arrays.asList(QueueVerticle.class, ClientVerticle.class);

    @Override
    public void start() {
        JsonObject config = config();
        Router router = Router.router(vertx);

        //Registering Codecs
        getVertx()
                .eventBus()
                .registerCodec(new DefMessageCodec<Client>(Client.class))
                .registerCodec(new DefMessageCodec<EventBusRequest>(EventBusRequest.class));

        VERTICLES.forEach(verticle ->
                vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config), handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("{} Verticle deployed successfully", verticle.getName());
                    } else {
                        LOGGER.error("Failed to deployed vertifle {}. Stopping server now", verticle.getName());
                        System.exit(1);
                    }
                })
        );

        router.route().handler(BodyHandler.create());

        router.get("/test").handler(routingContext -> routingContext.response().end("Hello guys."));

        router.post("/mbq/registerClient").handler(ClientHandler.registerHandler());
        router.post("/mbq/pull").handler(QueueHandler.pullHandler());
        router.post("/mbq/push").handler(QueueHandler.pushHandler());
        router.post("/mbq/commit").handler(QueueHandler.commitHandler());
        router.post("/mbq/rollback").handler(QueueHandler.rollbackHandler());
        router.post("/mbq/updateStatus").handler(QueueHandler.updateStatusHandler());
        router.post("/mbq/ping").handler(ClientHandler.heartbeatHandler());

        //TODO: Queue GUI Interface
        //router.route().handler(StaticHandler.create(config().getString(WEB_ROOT)));
        vertx.createHttpServer().requestHandler(router).listen(config.getInteger("web.port"));
    }
}
