package com.rags.tools.mbq.server.rest.websocket;

import com.rags.tools.mbq.server.rest.JsonUtil;
import com.rags.tools.mbq.stats.MBQStatsService;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;

public class WSHandler implements Handler<ServerWebSocket> {

    private final MBQStatsService statsService;

    public WSHandler(String statsCollectorClass) {
        this.statsService = MBQStatsService.getInstance(statsCollectorClass);
    }

    @Override
    public void handle(ServerWebSocket ctx) {
        if (ctx.path().equals("/mbq/ws")) {
            ctx.textMessageHandler(msg -> {
                try {
                    WebSocketRequest request = Json.decodeValue(msg, WebSocketRequest.class);
                    if (request.getType() == WebSocketRequestType.ALL_STATS) {
                        ctx.writeTextMessage(Json.encode(new WebSocketResponse<>(WebSocketRequestType.ALL_STATS, this.statsService.getCollectedStats())));
                    } else {
                        ctx.writeTextMessage(Json.encode(new WebSocketResponse<>(WebSocketRequestType.EXCEPTION, "Request is not supported")));
                    }
                } catch (Exception e) {
                    ctx.writeTextMessage(JsonUtil.createFailedResponse(msg + " is not in proper Format").encode());
                }
            }).closeHandler(handle -> System.out.println("Connection Closed")).exceptionHandler(e -> {
                System.out.println("exception occured");
                e.printStackTrace();
            });
        } else {
            ctx.reject();
        }
    }
}
