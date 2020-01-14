package com.rags.tools.mbq.endpoint.server.messagecodec;

public class EventBusRequest {
    private final String remoteHost;
    private final String cookie;
    private final Object reqObj;

    public EventBusRequest(String remoteHost, String cookie, Object reqObj) {
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

    public Object getReqObj() {
        return reqObj;
    }
}
