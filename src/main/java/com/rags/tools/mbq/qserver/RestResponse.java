package com.rags.tools.mbq.qserver;

public class RestResponse<T> {
    private T data;
    private boolean success;
    private boolean failed;
    private boolean warning;
    private String message;

    public void setData(T data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isWarning() {
        return warning;
    }

    public String getMessage() {
        return message;
    }
}
