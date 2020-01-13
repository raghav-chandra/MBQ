package com.rags.tools.mbq.endpoint.server;

public enum ErrorMessage {
    CODE_8000(8000, "Client should have proper name, queueName and batch information"),
    CODE_8001(8001, "Failed during client registration. "),
    CODE_8002(8002, "Failed to register heart beat.")
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
