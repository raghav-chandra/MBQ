package com.rags.tools.mbq.endpoint.server;

import com.rags.tools.mbq.endpoint.server.verticle.QueueVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;


public class MBQServerStartup extends AbstractVerticle {
    @Override
    public void start() {
        JsonObject config = config();
        Router router = Router.router(vertx);

        vertx.deployVerticle(QueueVerticle.class, new DeploymentOptions().setConfig(config), handler -> {
            if (handler.succeeded()) {
                System.out.println("Deployed verticle successfully");
            } else {
                handler.cause().printStackTrace();
            }
        });

        router.route().handler(BodyHandler.create());


        /*router.post("/mbq/registerClient").handler(UseCaseService.createUCSHandler());
        router.post("/mbq/pull").handler(UseCaseService.createUCSHandler());
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
