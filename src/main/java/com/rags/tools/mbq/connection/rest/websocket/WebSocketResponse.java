package com.rags.tools.mbq.connection.rest.websocket;

public class WebSocketResponse<T> {
    private WebSocketRequestType type;
    private T response;

    public WebSocketResponse(WebSocketRequestType type, T response) {
        this.type = type;
        this.response = response;
    }

    public WebSocketRequestType getType() {
        return type;
    }

    public T getResponse() {
        return response;
    }
}