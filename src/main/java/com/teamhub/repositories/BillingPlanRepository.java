package com.teamhub.repositories;

import com.teamhub.common.mongo.MongoRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class BillingPlanRepository extends MongoRepository {

    public BillingPlanRepository(MongoClient mongoClient) {
        super(mongoClient, "billing_plans");
    }

    public Future<JsonObject> findByTier(String tier) {
        JsonObject query = withNotDeleted(new JsonObject().put("tier", tier));
        return mongoClient.findOne(collectionName, query, null);
    }
}
