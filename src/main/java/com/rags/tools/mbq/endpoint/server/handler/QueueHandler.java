package com.rags.tools.mbq.endpoint.server.handler;

import com.rags.tools.mbq.endpoint.server.RequestType;
import com.rags.tools.mbq.endpoint.server.messagecodec.PushRequest;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class QueueHandler {
    public static Handler<RoutingContext> pullHandler() {
        return new AbstractRequestHandler<JsonObject, JsonObject>(RequestType.PULL_MESSAGES) {
            @Override
            protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? body.toJsonObject() : new JsonObject();
            }
        };
    }

    public static Handler<RoutingContext> updateStatusHandler() {
        return null;
    }

    public static Handler<RoutingContext> rollbackHandler() {
        return null;
    }

    public static Handler<RoutingContext> commitHandler() {
        return null;
    }

    public static Handler<RoutingContext> pushHandler() {
        return null;
    }
}
