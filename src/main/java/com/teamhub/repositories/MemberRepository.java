package com.teamhub.repositories;

import com.teamhub.common.mongo.MongoRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class MemberRepository extends MongoRepository {

    public MemberRepository(MongoClient mongoClient) {
        super(mongoClient, "members");
    }

    public Future<List<JsonObject>> findByOrganization(String organizationId, int skip, int limit) {
        JsonObject query = new JsonObject().put("organizationId", organizationId);
        JsonObject sort = new JsonObject().put("joinedAt", -1);
        return findAll(query, sort, skip, limit);
    }

    public Future<Long> countByOrganization(String organizationId) {
        JsonObject query = new JsonObject().put("organizationId", organizationId);
        return count(query);
    }

    public Future<JsonObject> findByEmail(String email, String organizationId) {
        JsonObject query = withNotDeleted(new JsonObject()
                .put("email", email)
                .put("organizationId", organizationId));
        return mongoClient.findOne(collectionName, query, null);
    }

    public Future<List<JsonObject>> findByRole(String organizationId, String role, int skip, int limit) {
        JsonObject query = new JsonObject()
                .put("organizationId", organizationId)
                .put("role", role);
        JsonObject sort = new JsonObject().put("joinedAt", -1);
        return findAll(query, sort, skip, limit);
    }
}
