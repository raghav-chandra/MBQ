package com.rags.tools.mbq.server.rest.handler;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.server.rest.messagecodec.PushRequest;
import com.rags.tools.mbq.message.MBQMessage;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class QueueHandler {
    public static Handler<RoutingContext> pullHandler() {
        return new AbstractRequestHandler<Client, List<MBQMessage>>(RequestType.PULL_MESSAGES) {
            @Override
            protected Client getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? Json.decodeValue(body, Client.class) : new Client(null);
            }
        };
    }

    public static Handler<RoutingContext> pushHandler() {
        return new AbstractRequestHandler<PushRequest, List<MBQMessage>>(RequestType.PUSH_MESSAGES) {
            @Override
            protected PushRequest getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? Json.decodeValue(body, PushRequest.class) : new PushRequest(null, null);
            }
        };
    }

    public static Handler<RoutingContext> updateStatusHandler() {
        return null;
    }

    public static Handler<RoutingContext> rollbackHandler() {
        return createCommitRollbackHandler(RequestType.REQUEST_ROLLBACK);
    }

    public static Handler<RoutingContext> commitHandler() {
        return createCommitRollbackHandler(RequestType.REQUEST_COMMIT);
    }

    private static Handler<RoutingContext> createCommitRollbackHandler(RequestType requestRollback) {
        return new AbstractRequestHandler<CommitRollbackRequest, Boolean>(requestRollback) {
            @Override
            protected CommitRollbackRequest getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? Json.decodeValue(body, CommitRollbackRequest.class) : new CommitRollbackRequest(null, null);
            }
        };
    }
}
