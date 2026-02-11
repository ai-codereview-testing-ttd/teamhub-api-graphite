package com.teamhub.common.mongo;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.time.Instant;
import java.util.List;

public abstract class MongoRepository {

    protected final MongoClient mongoClient;
    protected final String collectionName;

    protected MongoRepository(MongoClient mongoClient, String collectionName) {
        this.mongoClient = mongoClient;
        this.collectionName = collectionName;
    }

    /**
     * Adds the soft-delete filter to any query: only return documents where deletedAt is null.
     */
    protected JsonObject withNotDeleted(JsonObject query) {
        return query.copy().put("deletedAt", (Object) null);
    }

    public Future<JsonObject> findById(String id) {
        JsonObject query = withNotDeleted(new JsonObject().put("_id", id));
        return mongoClient.findOne(collectionName, query, null);
    }

    public Future<List<JsonObject>> findAll(JsonObject query, JsonObject sort, int skip, int limit) {
        JsonObject safeQuery = withNotDeleted(query);
        FindOptions options = new FindOptions()
                .setSort(sort)
                .setSkip(skip)
                .setLimit(limit);
        return mongoClient.findWithOptions(collectionName, safeQuery, options);
    }

    public Future<Long> count(JsonObject query) {
        JsonObject safeQuery = withNotDeleted(query);
        return mongoClient.count(collectionName, safeQuery);
    }

    public Future<String> insert(JsonObject document) {
        document.put("createdAt", Instant.now().toString());
        document.put("updatedAt", Instant.now().toString());
        document.put("deletedAt", (Object) null);
        return mongoClient.insert(collectionName, document);
    }

    public Future<Void> update(String id, JsonObject update) {
        JsonObject query = withNotDeleted(new JsonObject().put("_id", id));
        JsonObject updateDoc = new JsonObject().put("$set",
                update.copy().put("updatedAt", Instant.now().toString()));
        return mongoClient.updateCollection(collectionName, query, updateDoc)
                .mapEmpty();
    }

    public Future<Void> softDelete(String id) {
        JsonObject query = new JsonObject().put("_id", id);
        JsonObject update = new JsonObject().put("$set",
                new JsonObject().put("deletedAt", Instant.now().toString()));
        return mongoClient.updateCollection(collectionName, query, update)
                .mapEmpty();
    }
}
