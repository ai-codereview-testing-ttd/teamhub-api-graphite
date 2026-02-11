package com.teamhub.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class SecurityHeaderHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response()
                .putHeader("X-Content-Type-Options", "nosniff")
                .putHeader("X-Frame-Options", "DENY")
                .putHeader("X-XSS-Protection", "1; mode=block")
                .putHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .putHeader("Content-Security-Policy", "default-src 'self'");

        ctx.next();
    }
}
