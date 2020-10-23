package com.rags.tools.mbq.connection.rest;

import com.rags.tools.mbq.connection.rest.handler.ClientHandler;
import com.rags.tools.mbq.connection.rest.handler.MBQueueDSHandler;
import com.rags.tools.mbq.connection.rest.handler.QueueHandler;
import com.rags.tools.mbq.connection.rest.messagecodec.DefMessageCodec;
import com.rags.tools.mbq.connection.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.connection.rest.verticle.ClientVerticle;
import com.rags.tools.mbq.connection.rest.verticle.DataStoreVerticle;
import com.rags.tools.mbq.connection.rest.verticle.QueueVerticle;
import com.rags.tools.mbq.connection.rest.websocket.WSHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class MBQServerStartup extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQServerStartup.class);
    private static final List<Class<? extends AbstractVerticle>> VERTICLES = Arrays.asList(QueueVerticle.class, ClientVerticle.class, DataStoreVerticle.class);

    private static final String STATS_COLLECTOR = "stats.collector";
    private static final String WEB_ROOT = "web.root";
    private static final String WEB_PORT = "web.port";

    @Override
    public void start() {
        JsonObject config = config();
        Router router = Router.router(vertx);

        //Registering Codecs
        getVertx().eventBus().registerCodec(new DefMessageCodec<>(EventBusRequest.class));

        //Registering Workers
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

        router.get("/test").handler(routingContext -> routingContext.response().end("MBQ is up and running"));

        router.post("/mbq/registerClient").handler(ClientHandler.registerHandler());
        router.post("/mbq/pull").handler(QueueHandler.pullHandler());
        router.post("/mbq/push").handler(QueueHandler.pushHandler());
        router.post("/mbq/commit").handler(QueueHandler.commitHandler());
        router.post("/mbq/rollback").handler(QueueHandler.rollbackHandler());
        router.post("/mbq/ping").handler(ClientHandler.heartbeatHandler());

        router.post("/mbq/console/get").handler(MBQueueDSHandler.getHandler());
        router.post("/mbq/console/search").handler(MBQueueDSHandler.searchHandler());
        router.post("/mbq/console/updateStatus").handler(QueueHandler.updateStatusHandler());

        //Queue GUI Interface for Stats and Console
        router.route().handler(StaticHandler.create(config().getString(WEB_ROOT)));

        //Staring Http Server and WS server
        vertx.createHttpServer()
                .websocketHandler(new WSHandler(config.getString(STATS_COLLECTOR, "com.rags.tools.mbq.stats.collectors.NoOpStatsCollector")))
                .requestHandler(router).listen(config.getInteger(WEB_PORT));

        LOGGER.info("MBQ started at {} port no", config.getInteger(WEB_PORT));
    }
}
