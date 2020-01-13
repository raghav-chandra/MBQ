package com.rags.tools.mbq.endpoint.server.handler;

import com.rags.tools.mbq.endpoint.server.RequestType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ClientHandler {
    /**
     * Registers Client
     *
     * @return Client information back with ID
     */
    public static Handler<RoutingContext> registerHandler() {
        return new AbstractRequestHandler<JsonObject, JsonObject>(RequestType.REGISTER_CLIENT) {
            @Override
            protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? body.toJsonObject() : new JsonObject();
            }
        };
    }

    public static Handler<RoutingContext> heartbeatHandler() {
        return new AbstractRequestHandler<JsonObject, JsonObject>(RequestType.REGISTER_HEARTBEAT) {
            @Override
            protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? body.toJsonObject() : new JsonObject();
            }
        };
    }
}
