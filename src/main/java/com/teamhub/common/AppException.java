package com.teamhub.common;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final int statusCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.statusCode = errorCode.getStatusCode();
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = errorCode.getStatusCode();
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = errorCode.getStatusCode();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
