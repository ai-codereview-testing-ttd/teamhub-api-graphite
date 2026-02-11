package com.teamhub.managers;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder notification manager. Currently logs all notifications.
 * In production, this would send webhook/email notifications.
 */
public class NotificationManager {

    private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    public Future<Void> notifyMemberInvited(String email, String organizationName) {
        logger.info("NOTIFICATION: Member invited - {} to {}", email, organizationName);
        return Future.succeededFuture();
    }

    public Future<Void> notifyMemberRemoved(String email, String organizationName) {
        logger.info("NOTIFICATION: Member removed - {} from {}", email, organizationName);
        return Future.succeededFuture();
    }

    public Future<Void> notifyTaskAssigned(String assigneeId, String taskTitle, String projectName) {
        logger.info("NOTIFICATION: Task assigned - {} to user {} in project {}", taskTitle, assigneeId, projectName);
        return Future.succeededFuture();
    }

    public Future<Void> notifyTaskStatusChanged(String taskTitle, String oldStatus, String newStatus) {
        logger.info("NOTIFICATION: Task status changed - {} from {} to {}", taskTitle, oldStatus, newStatus);
        return Future.succeededFuture();
    }

    public Future<Void> sendWebhook(String url, JsonObject payload) {
        logger.info("WEBHOOK: Sending to {} with payload: {}", url, payload.encode());
        return Future.succeededFuture();
    }
}
