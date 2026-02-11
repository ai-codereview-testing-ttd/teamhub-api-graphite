package com.teamhub.middleware;

import com.nimbusds.jwt.JWTClaimsSet;
import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.utils.JwtHelper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;

public class AuthHandler implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/health",
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );

    @Override
    public void handle(RoutingContext ctx) {
        String path = ctx.normalizedPath();

        // Skip authentication for public routes
        if (PUBLIC_PATHS.contains(path)) {
            ctx.next();
            return;
        }

        String authHeader = ctx.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.fail(new AppException(ErrorCode.UNAUTHORIZED, "Missing or invalid Authorization header"));
            return;
        }

        String token = authHeader.substring(7);
        JWTClaimsSet claims = JwtHelper.validateToken(token);

        if (claims == null) {
            ctx.fail(new AppException(ErrorCode.UNAUTHORIZED, "Invalid or expired token"));
            return;
        }

        try {
            String userId = claims.getSubject();
            String email = claims.getStringClaim("email");
            String organizationId = claims.getStringClaim("organizationId");

            ctx.put("userId", userId);
            ctx.put("email", email);
            ctx.put("organizationId", organizationId);

            logger.debug("Authenticated user: {} (org: {})", userId, organizationId);
            ctx.next();
        } catch (ParseException e) {
            logger.error("Failed to extract claims from JWT", e);
            ctx.fail(new AppException(ErrorCode.UNAUTHORIZED, "Invalid token claims"));
        }
    }
}
