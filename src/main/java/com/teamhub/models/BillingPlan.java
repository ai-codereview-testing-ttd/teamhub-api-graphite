package com.teamhub.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingPlan {

    public enum Tier {
        FREE, STARTER, PROFESSIONAL, ENTERPRISE
    }

    private String id;
    private String name;
    private Tier tier;
    private int maxMembers;
    private int maxProjects;
    private double pricePerMonth;
    @Builder.Default
    private List<String> features = new ArrayList<>();

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("tier", tier != null ? tier.name() : null)
                .put("maxMembers", maxMembers)
                .put("maxProjects", maxProjects)
                .put("pricePerMonth", pricePerMonth)
                .put("features", new JsonArray(features != null ? features : new ArrayList<>()));
    }

    public static BillingPlan fromJson(JsonObject json) {
        if (json == null) return null;
        List<String> features = new ArrayList<>();
        JsonArray arr = json.getJsonArray("features");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                features.add(arr.getString(i));
            }
        }
        return BillingPlan.builder()
                .id(json.getString("_id", json.getString("id")))
                .name(json.getString("name"))
                .tier(json.getString("tier") != null ? Tier.valueOf(json.getString("tier")) : Tier.FREE)
                .maxMembers(json.getInteger("maxMembers", 5))
                .maxProjects(json.getInteger("maxProjects", 3))
                .pricePerMonth(json.getDouble("pricePerMonth", 0.0))
                .features(features)
                .build();
    }
}
