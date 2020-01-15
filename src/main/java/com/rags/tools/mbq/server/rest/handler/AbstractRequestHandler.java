package com.rags.tools.mbq.server.rest.handler;

import com.rags.tools.mbq.server.rest.JsonUtil;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.EventBusRequest;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by ragha on 22-04-2018.
 */
public abstract class AbstractRequestHandler<S, T> implements Handler<RoutingContext> {

    protected final static String COOKIE_STRING = "Cookie";

    private final RequestType requestType;

    public AbstractRequestHandler(RequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        EventBus eventBus = context.vertx().eventBus();

        String cookie = request.headers().get(COOKIE_STRING);
        S requestData = getRequestData(request, context.getBody());
        EventBusRequest reqObject = new EventBusRequest(context.request().remoteAddress().host(), cookie, requestData);
        handleFuture(request, createFuture(requestType, eventBus, reqObject), eventBus, cookie);
    }

    protected void handleFuture(HttpServerRequest request, Future<T> future, EventBus eventBus, String cookie) {
        future.setHandler(handler -> {
            if (handler.succeeded()) {
                onSuccess(request, handler.result());
            } else {
                onFailure(request, handler.cause());
            }
        });
    }

    void onSuccess(HttpServerRequest request, T result) {
        request.response().end(JsonUtil.createSuccessResponse(result).encodePrettily());
    }

    void onFailure(HttpServerRequest request, Throwable cause) {
        request.response().end(JsonUtil.createFailedResponse(cause.getMessage()).encodePrettily());
    }

    private Future<T> createFuture(RequestType requestType, EventBus eventBus, EventBusRequest reqObject) {
        Future<T> future = Future.future();
        eventBus.<T>send(requestType.name(), reqObject, reply -> {
            if (reply.succeeded()) {
                future.complete(reply.result().body());
            } else {
                future.fail(reply.cause());
            }
        });
        return future;
    }

    protected abstract S getRequestData(HttpServerRequest request, Buffer body);
}