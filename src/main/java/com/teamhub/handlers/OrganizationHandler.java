package com.teamhub.handlers;

import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.managers.OrganizationManager;
import com.teamhub.utils.ValidationHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationHandler.class);

    private final OrganizationManager organizationManager;

    public OrganizationHandler(OrganizationManager organizationManager) {
        this.organizationManager = organizationManager;
    }

    public void mount(Router router) {
        router.get("/organizations/:id").handler(this::getOrganization);
        router.put("/organizations/:id").handler(this::updateOrganization);
        router.put("/organizations/:id/settings").handler(this::updateSettings);
    }

    private void getOrganization(RoutingContext ctx) {
        String organizationId = ctx.pathParam("id");

        organizationManager.getOrganization(organizationId)
                .onSuccess(org -> sendJson(ctx, 200, org.toJson()))
                .onFailure(ctx::fail);
    }

    private void updateOrganization(RoutingContext ctx) {
        String organizationId = ctx.pathParam("id");
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.fail(new AppException(ErrorCode.BAD_REQUEST, "Request body is required"));
            return;
        }

        organizationManager.updateOrganization(organizationId, body)
                .onSuccess(org -> sendJson(ctx, 200, org.toJson()))
                .onFailure(ctx::fail);
    }

    private void updateSettings(RoutingContext ctx) {
        String organizationId = ctx.pathParam("id");
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.fail(new AppException(ErrorCode.BAD_REQUEST, "Request body is required"));
            return;
        }

        organizationManager.updateSettings(organizationId, body)
                .onSuccess(org -> sendJson(ctx, 200, org.toJson()))
                .onFailure(ctx::fail);
    }

    private void sendJson(RoutingContext ctx, int statusCode, JsonObject body) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(body.encode());
    }
}
