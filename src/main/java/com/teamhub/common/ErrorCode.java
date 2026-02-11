package com.teamhub.common;

public enum ErrorCode {

    NOT_FOUND("Resource not found", 404),
    UNAUTHORIZED("Unauthorized", 401),
    FORBIDDEN("Forbidden", 403),
    BAD_REQUEST("Bad request", 400),
    CONFLICT("Conflict", 409),
    INTERNAL_ERROR("Internal server error", 500),
    VALIDATION_ERROR("Validation error", 422);

    private final String message;
    private final int statusCode;

    ErrorCode(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
