package com.rags.tools.mbq.connection.rest;

public enum RequestType {
    REGISTER_CLIENT,
    VALIDATE_CLIENT,

    PUSH_MESSAGES,

    PULL_MESSAGES,

    REGISTER_HEARTBEAT,

    REQUEST_COMMIT,
    REQUEST_ROLLBACK,

    UPDATE_STATUS,


    LOOKUP_ITEMS,
    GET_ITEMS

}