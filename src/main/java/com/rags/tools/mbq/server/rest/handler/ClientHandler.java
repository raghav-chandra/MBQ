package com.rags.tools.mbq.server.rest.handler;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.server.rest.RequestType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class ClientHandler {
    /**
     * Registers Client
     *
     * @return Client information back with ID
     */
    public static Handler<RoutingContext> registerHandler() {
        return createClientHandler(RequestType.REGISTER_CLIENT);
    }

    /**
     * Registers Heartbeat of client
     *
     * @return Client with Heartbeat information
     */
    public static Handler<RoutingContext> heartbeatHandler() {
        return createClientHandler(RequestType.REGISTER_HEARTBEAT);
    }

    private static Handler<RoutingContext> createClientHandler(RequestType requestType) {
        return new AbstractRequestHandler<Client, Client>(requestType) {
            @Override
            protected Client getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? Json.decodeValue(body, Client.class) : new Client();
            }
        };
    }
}
