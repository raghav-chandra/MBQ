package com.rags.tools.mbq.connection.rest.handler;

import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.connection.rest.RequestType;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class MBQueueDSHandler {

    public static Handler<RoutingContext> searchHandler() {
        return new AbstractRequestHandler<SearchRequest, List<MBQMessage>>(RequestType.LOOKUP_ITEMS) {
            @Override
            protected SearchRequest getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? Json.decodeValue(body, SearchRequest.class) : new SearchRequest();
            }
        };
    }

    public static Handler<RoutingContext> getHandler() {
        return new AbstractRequestHandler<List<String>, List<MBQMessage>>(RequestType.GET_ITEMS) {
            @Override
            protected List<String> getRequestData(HttpServerRequest request, Buffer body) {
                return body != null ? (List<String>) Json.decodeValue(body, List.class) : List.of();
            }
        };
    }

}
