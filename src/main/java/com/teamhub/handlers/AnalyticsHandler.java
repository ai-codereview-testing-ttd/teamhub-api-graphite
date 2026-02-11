package com.teamhub.handlers;

import com.teamhub.managers.AnalyticsManager;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsHandler.class);

    private final AnalyticsManager analyticsManager;

    public AnalyticsHandler(AnalyticsManager analyticsManager) {
        this.analyticsManager = analyticsManager;
    }

    public void mount(Router router) {
        router.get("/analytics/dashboard").handler(this::getDashboard);
        router.get("/analytics/tasks").handler(this::getTaskAnalytics);
        router.get("/analytics/members").handler(this::getMemberAnalytics);
    }

    private void getDashboard(RoutingContext ctx) {
        String organizationId = ctx.get("organizationId");

        analyticsManager.getDashboard(organizationId)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(ctx::fail);
    }

    private void getTaskAnalytics(RoutingContext ctx) {
        String organizationId = ctx.get("organizationId");

        analyticsManager.getTaskAnalytics(organizationId)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(ctx::fail);
    }

    private void getMemberAnalytics(RoutingContext ctx) {
        String organizationId = ctx.get("organizationId");

        analyticsManager.getMemberAnalytics(organizationId)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(ctx::fail);
    }

    private void sendJson(RoutingContext ctx, int statusCode, JsonObject body) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(body.encode());
    }
}
