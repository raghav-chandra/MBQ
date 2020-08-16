package com.rags.tools.mbq.server.rest.handler;

import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.server.rest.RequestType;
import com.rags.tools.mbq.server.rest.messagecodec.SearchRequest;
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

}
