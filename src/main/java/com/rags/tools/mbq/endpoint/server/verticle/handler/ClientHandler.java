package com.rags.tools.mbq.endpoint.server.verticle.handler;

import com.rags.tools.mbq.endpoint.server.RequestType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ClientHandler {
    public static Handler<RoutingContext> registerHandler() {
        return new AbstractRequestHandler<JsonObject, JsonObject>("", RequestType.REGISTER_CLIENT) {
            @Override
            protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
                return null;
            }
        };
    }
}
