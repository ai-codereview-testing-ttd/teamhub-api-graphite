package com.teamhub.middleware;

import com.teamhub.common.AppException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @Override
    public void handle(RoutingContext ctx) {
        Throwable failure = ctx.failure();

        if (failure == null) {
            int statusCode = ctx.statusCode() >= 400 ? ctx.statusCode() : 500;
            sendError(ctx, statusCode, "UNKNOWN_ERROR", "An unexpected error occurred");
            return;
        }

        if (failure instanceof AppException appException) {
            logger.warn("Application error: {} - {}", appException.getErrorCode(), appException.getMessage());
            sendError(ctx, appException.getStatusCode(),
                    appException.getErrorCode().name(), appException.getMessage());
        } else {
            logger.error("Unexpected error", failure);
            sendError(ctx, 500, "INTERNAL_ERROR", "An internal server error occurred");
        }
    }

    private void sendError(RoutingContext ctx, int statusCode, String errorCode, String message) {
        JsonObject errorBody = new JsonObject()
                .put("error", errorCode)
                .put("message", message)
                .put("statusCode", statusCode);

        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(errorBody.encode());
    }
}
