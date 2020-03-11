package com.rags.tools.mbq.server.rest.messagecodec;

public class EventBusRequest {
    private String remoteHost;
    private String cookie;
    private Object reqObj;

    public EventBusRequest() {
    }

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
