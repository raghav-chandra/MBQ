package com.rags.tools.mbq.connection.rest.websocket;

public class WebSocketRequest {

    private WebSocketRequestType type;

    public WebSocketRequestType getType() {
        return type;
    }

    public WebSocketRequest setType(WebSocketRequestType type) {
        this.type = type;
        return this;
    }
}