package com.rags.tools.mbq.endpoint.server;

public enum RequestType {
    REGISTER_CLIENT,
    VALIDATE_CLIENT,

    PUSH_MESSAGES,

    PULL_MESSAGES,

    REGISTER_HEARTBEAT,

    REQUEST_COMMIT,
    REQUEST_ROLLBACK,

    UPDATE_STATUS

}