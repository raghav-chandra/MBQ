package com.rags.tools.mbq.connection.rest;

public enum ErrorMessage {
    CLIENT_INVALID(9000, "Client should have proper name, queueName and batch information"),

    CLIENT_REGISTER_FAILED(9001, "Failed during client registration. "),
    PING_REGISTER_FAILED(9002, "Failed to register heart beat."),

    MESSAGES_INVALID(9003, "Message is not present"),

    MESSAGES_NOT_FOUND_FOR_COMMIT(9004, "Message not found for committing"),
    MESSAGES_NOT_FOUND_FOR_ROLLBACK(9005, "Message not found for rollback"),

    MESSAGE_PUBLISHING_FAILED(9006, "Message publishing failed"),
    MESSAGE_COMMIT_FAILED(9007, "Message commit failed"),
    MESSAGE_ROLLBACK_FAILED(9008, "Message rollback failed"),
    MESSAGE_PULL_FAILED(9009, "Message pull failed"),


    INVALID_SEARCH_REQUEST(12000, " Invalid Search request"),
    FAILED_SEARCH_REQUEST(12001, "Search Request failed. "),
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
