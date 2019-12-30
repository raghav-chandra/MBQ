package com.rags.tools.mbq.exception;

import java.io.IOException;

public class MBQException extends RuntimeException {

    public MBQException(String message) {
        super(message);
    }

    public MBQException(String message, Throwable cause) {
        super(message, cause);
    }

}
