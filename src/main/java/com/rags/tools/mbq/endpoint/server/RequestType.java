package com.rags.tools.mbq.endpoint.server;

public enum RequestType {
    REGISTER_CLIENT,

    PUSH_MESSAGES,

    PULL_MESSAGES,

    REGISTER_HEARTBEAT,

    COMMIT,
    ROLLBACK,

    UPDATE_STATUS

}