package com.teamhub.models;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    private String id;
    private String name;
    private String slug;
    private String billingPlanId;
    private int memberCount;
    private JsonObject settings;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("slug", slug)
                .put("billingPlanId", billingPlanId)
                .put("memberCount", memberCount)
                .put("settings", settings != null ? settings : new JsonObject())
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt)
                .put("deletedAt", deletedAt);
    }

    public static Organization fromJson(JsonObject json) {
        if (json == null) return null;
        return Organization.builder()
                .id(json.getString("_id", json.getString("id")))
                .name(json.getString("name"))
                .slug(json.getString("slug"))
                .billingPlanId(json.getString("billingPlanId"))
                .memberCount(json.getInteger("memberCount", 0))
                .settings(json.getJsonObject("settings", new JsonObject()))
                .createdAt(json.getString("createdAt"))
                .updatedAt(json.getString("updatedAt"))
                .deletedAt(json.getString("deletedAt"))
                .build();
    }
}
