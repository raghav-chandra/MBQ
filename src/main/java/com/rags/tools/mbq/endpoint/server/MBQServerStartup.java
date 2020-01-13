package com.rags.tools.mbq.endpoint.server;

import com.rags.tools.mbq.endpoint.server.handler.QueueHandler;
import com.rags.tools.mbq.endpoint.server.verticle.ClientVerticle;
import com.rags.tools.mbq.endpoint.server.verticle.QueueVerticle;
import com.rags.tools.mbq.endpoint.server.handler.ClientHandler;
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

        router.post("/mbq/registerClient").handler(ClientHandler.registerHandler());
        router.post("/mbq/pull").handler(QueueHandler.pullHandler());
        router.post("/mbq/push").handler(QueueHandler.pushHandler());
        router.post("/mbq/commit").handler(QueueHandler.commitHandler());
        router.post("/mbq/rollback").handler(QueueHandler.rollbackHandler());
        router.post("/mbq/updateStatus").handler(QueueHandler.updateStatusHandler());
        router.post("/mbq/ping").handler(ClientHandler.heartbeatHandler());


        /*router.post("/mbq/pull").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/commit").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/rollback").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/push").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/ping").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/updateStatus").handler(UseCaseService.createUCSHandler());*/

        //TODO: Cache GUI Interface
//        router.route().handler(StaticHandler.create(config().getString(WEB_ROOT)));
        vertx.createHttpServer().requestHandler(router).listen(8642);
    }
}
