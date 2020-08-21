package com.rags.tools.mbq.server.rest.messagecodec;

public class EventBusRequest<T> {
    private String remoteHost;
    private String cookie;
    private T reqObj;

    public EventBusRequest() {
    }

    public EventBusRequest(String remoteHost, String cookie, T reqObj) {
        this.remoteHost = remoteHost;
        this.cookie = cookie;
        this.reqObj = reqObj;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getCookie() {
        return cookie;
    }

    public T getReqObj() {
        return reqObj;
    }
}
