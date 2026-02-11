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
public class Project {

    public enum Status {
        ACTIVE, ARCHIVED, COMPLETED
    }

    private String id;
    private String name;
    private String description;
    private String organizationId;
    private Status status;
    @Builder.Default
    private List<String> memberIds = new ArrayList<>();
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private String createdBy;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("description", description)
                .put("organizationId", organizationId)
                .put("status", status != null ? status.name() : null)
                .put("memberIds", new JsonArray(memberIds != null ? memberIds : new ArrayList<>()))
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt)
                .put("deletedAt", deletedAt)
                .put("createdBy", createdBy);
        return json;
    }

    public static Project fromJson(JsonObject json) {
        if (json == null) return null;
        List<String> memberIds = new ArrayList<>();
        JsonArray arr = json.getJsonArray("memberIds");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                memberIds.add(arr.getString(i));
            }
        }
        return Project.builder()
                .id(json.getString("_id", json.getString("id")))
                .name(json.getString("name"))
                .description(json.getString("description"))
                .organizationId(json.getString("organizationId"))
                .status(json.getString("status") != null ? Status.valueOf(json.getString("status")) : Status.ACTIVE)
                .memberIds(memberIds)
                .createdAt(json.getString("createdAt"))
                .updatedAt(json.getString("updatedAt"))
                .deletedAt(json.getString("deletedAt"))
                .createdBy(json.getString("createdBy"))
                .build();
    }
}
