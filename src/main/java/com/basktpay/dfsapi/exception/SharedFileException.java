package com.basktpay.dfsapi.exception;

public class SharedFileException  extends RuntimeException {

    private String message;

    public SharedFileException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
