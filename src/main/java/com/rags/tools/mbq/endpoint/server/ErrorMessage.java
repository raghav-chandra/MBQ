package com.rags.tools.mbq.endpoint.server;

public enum ErrorMessage {
    CLIENT_INVALID(9000, "Client should have proper name, queueName and batch information"),

    CLIENT_REGISTER_FAILED(9001, "Failed during client registration. "),
    PING_REGISTER_FAILED(9002, "Failed to register heart beat."),

    MESSAGES_INVALID(9003, "Message is not present"),
    ;

    private final int code;
    private final String message;

    ErrorMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
