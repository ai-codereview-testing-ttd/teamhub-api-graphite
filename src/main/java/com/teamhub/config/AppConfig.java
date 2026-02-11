package com.teamhub.config;

public final class AppConfig {

    private AppConfig() {
        // Utility class
    }

    // MongoDB
    public static final String MONGO_CONNECTION_STRING = "mongodb://localhost:27017";
    public static final String MONGO_DATABASE = "teamhub";

    // JWT
    public static final String JWT_SECRET = "teamhub-dev-jwt-secret-key-change-in-production-min-256-bits-long";
    public static final String JWT_ISSUER = "teamhub-api";
    public static final int JWT_EXPIRY_SECONDS = 86400;

    // Server
    public static final int SERVER_PORT = 8080;

    // Webhook
    public static final String WEBHOOK_SIGNING_SECRET = "whsec_teamhub_dev_signing_key";

    // Pagination
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
}
