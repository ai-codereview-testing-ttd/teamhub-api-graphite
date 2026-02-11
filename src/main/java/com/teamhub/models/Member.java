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
public class Member {

    public enum Role {
        OWNER, ADMIN, MEMBER, VIEWER;

        public int rank() {
            return switch (this) {
                case OWNER -> 4;
                case ADMIN -> 3;
                case MEMBER -> 2;
                case VIEWER -> 1;
            };
        }

        public boolean isHigherThan(Role other) {
            return this.rank() > other.rank();
        }
    }

    private String id;
    private String email;
    private String name;
    private Role role;
    private String organizationId;
    private String avatarUrl;
    private String invitedAt;
    private String joinedAt;
    private String deletedAt;

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("email", email)
                .put("name", name)
                .put("role", role != null ? role.name() : null)
                .put("organizationId", organizationId)
                .put("avatarUrl", avatarUrl)
                .put("invitedAt", invitedAt)
                .put("joinedAt", joinedAt)
                .put("deletedAt", deletedAt);
    }

    public static Member fromJson(JsonObject json) {
        if (json == null) return null;
        return Member.builder()
                .id(json.getString("_id", json.getString("id")))
                .email(json.getString("email"))
                .name(json.getString("name"))
                .role(json.getString("role") != null ? Role.valueOf(json.getString("role")) : Role.MEMBER)
                .organizationId(json.getString("organizationId"))
                .avatarUrl(json.getString("avatarUrl"))
                .invitedAt(json.getString("invitedAt"))
                .joinedAt(json.getString("joinedAt"))
                .deletedAt(json.getString("deletedAt"))
                .build();
    }
}
