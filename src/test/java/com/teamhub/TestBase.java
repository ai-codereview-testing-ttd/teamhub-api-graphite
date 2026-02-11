package com.teamhub;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Base class for unit tests providing common test data builders and mock factories.
 */
public abstract class TestBase {

    protected static final String TEST_USER_ID = "user-001";
    protected static final String TEST_ORG_ID = "org-001";
    protected static final String TEST_EMAIL = "test@teamhub.com";

    /**
     * Fluent scenario builder for setting up test data and mocks.
     */
    public static class ScenarioSetup {

        private JsonObject project;
        private JsonObject task;
        private JsonObject member;
        private JsonObject organization;
        private JsonObject billingPlan;

        public ScenarioSetup withProject() {
            this.project = createTestProject(randomId(), TEST_ORG_ID);
            return this;
        }

        public ScenarioSetup withProject(String projectId) {
            this.project = createTestProject(projectId, TEST_ORG_ID);
            return this;
        }

        public ScenarioSetup withProject(String projectId, String orgId) {
            this.project = createTestProject(projectId, orgId);
            return this;
        }

        public ScenarioSetup withTask() {
            this.task = createTestTask(randomId(), project != null ? project.getString("_id") : randomId());
            return this;
        }

        public ScenarioSetup withTask(String taskId) {
            this.task = createTestTask(taskId, project != null ? project.getString("_id") : randomId());
            return this;
        }

        public ScenarioSetup withMember(String role) {
            this.member = createTestMember(randomId(), TEST_ORG_ID, role);
            return this;
        }

        public ScenarioSetup withMember(String memberId, String role) {
            this.member = createTestMember(memberId, TEST_ORG_ID, role);
            return this;
        }

        public ScenarioSetup withOrganization() {
            this.organization = createTestOrganization(TEST_ORG_ID);
            return this;
        }

        public ScenarioSetup withOrganization(String orgId) {
            this.organization = createTestOrganization(orgId);
            return this;
        }

        public ScenarioSetup withBillingPlan(String tier, int maxMembers, int maxProjects) {
            this.billingPlan = createTestBillingPlan(tier, maxMembers, maxProjects);
            return this;
        }

        public ScenarioSetup withFreePlan() {
            this.billingPlan = createTestBillingPlan("FREE", 5, 3);
            return this;
        }

        public JsonObject getProject() { return project; }
        public JsonObject getTask() { return task; }
        public JsonObject getMember() { return member; }
        public JsonObject getOrganization() { return organization; }
        public JsonObject getBillingPlan() { return billingPlan; }
    }

    protected static ScenarioSetup scenario() {
        return new ScenarioSetup();
    }

    protected static String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    public static JsonObject createTestProject(String id, String organizationId) {
        return new JsonObject()
                .put("_id", id)
                .put("name", "Test Project")
                .put("description", "A test project")
                .put("organizationId", organizationId)
                .put("status", "ACTIVE")
                .put("memberIds", new JsonArray().add(TEST_USER_ID))
                .put("createdBy", TEST_USER_ID)
                .put("createdAt", Instant.now().toString())
                .put("updatedAt", Instant.now().toString())
                .put("deletedAt", (Object) null);
    }

    public static JsonObject createTestTask(String id, String projectId) {
        return new JsonObject()
                .put("_id", id)
                .put("title", "Test Task")
                .put("description", "A test task")
                .put("projectId", projectId)
                .put("assigneeId", TEST_USER_ID)
                .put("status", "TODO")
                .put("priority", "MEDIUM")
                .put("dueDate", "2025-12-31")
                .put("tags", new JsonArray().add("test"))
                .put("createdBy", TEST_USER_ID)
                .put("createdAt", Instant.now().toString())
                .put("updatedAt", Instant.now().toString())
                .put("deletedAt", (Object) null);
    }

    public static JsonObject createTestMember(String id, String organizationId, String role) {
        return new JsonObject()
                .put("_id", id)
                .put("email", "member-" + id.substring(0, 6) + "@teamhub.com")
                .put("name", "Test Member")
                .put("role", role)
                .put("organizationId", organizationId)
                .put("avatarUrl", (Object) null)
                .put("invitedAt", Instant.now().toString())
                .put("joinedAt", Instant.now().toString())
                .put("deletedAt", (Object) null);
    }

    public static JsonObject createTestOrganization(String id) {
        return new JsonObject()
                .put("_id", id)
                .put("name", "Test Organization")
                .put("slug", "test-organization")
                .put("billingPlanId", "free")
                .put("memberCount", 1)
                .put("settings", new JsonObject())
                .put("createdAt", Instant.now().toString())
                .put("updatedAt", Instant.now().toString())
                .put("deletedAt", (Object) null);
    }

    public static JsonObject createTestBillingPlan(String tier, int maxMembers, int maxProjects) {
        return new JsonObject()
                .put("_id", tier.toLowerCase())
                .put("name", tier.substring(0, 1).toUpperCase() + tier.substring(1).toLowerCase())
                .put("tier", tier)
                .put("maxMembers", maxMembers)
                .put("maxProjects", maxProjects)
                .put("pricePerMonth", 0.0)
                .put("features", new JsonArray().add("Basic features"));
    }
}
