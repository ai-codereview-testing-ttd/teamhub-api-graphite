package com.teamhub.repositories;

import com.teamhub.common.mongo.MongoRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class OrganizationRepository extends MongoRepository {

    public OrganizationRepository(MongoClient mongoClient) {
        super(mongoClient, "organizations");
    }

    public Future<JsonObject> findBySlug(String slug) {
        JsonObject query = withNotDeleted(new JsonObject().put("slug", slug));
        return mongoClient.findOne(collectionName, query, null);
    }
}
