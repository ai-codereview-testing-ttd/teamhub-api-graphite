package com.teamhub.managers;

import com.teamhub.TestBase;
import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.models.BillingPlan;
import com.teamhub.models.Project;
import com.teamhub.repositories.ProjectRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class ProjectManagerTest extends TestBase {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private BillingManager billingManager;
    @Mock
    private MemberManager memberManager;

    private ProjectManager projectManager;

    @BeforeEach
    void setUp() {
        projectManager = new ProjectManager(projectRepository, billingManager, memberManager);
    }

    @Test
    void createProject_success(Vertx vertx, VertxTestContext ctx) {
        ScenarioSetup setup = scenario().withFreePlan();
        BillingPlan plan = BillingPlan.fromJson(setup.getBillingPlan());

        when(billingManager.getCurrentPlan(TEST_ORG_ID)).thenReturn(Future.succeededFuture(plan));
        when(projectRepository.countByOrganization(TEST_ORG_ID)).thenReturn(Future.succeededFuture(0L));
        when(projectRepository.insert(any(JsonObject.class))).thenReturn(Future.succeededFuture("proj-123"));

        JsonObject body = new JsonObject().put("name", "New Project").put("description", "Description");

        projectManager.createProject(body, TEST_USER_ID, TEST_ORG_ID)
                .onComplete(ctx.succeeding(project -> {
                    ctx.verify(() -> {
                        assertNotNull(project);
                        assertEquals("New Project", project.getName());
                        assertEquals(TEST_ORG_ID, project.getOrganizationId());
                        assertEquals(Project.Status.ACTIVE, project.getStatus());
                        verify(projectRepository).insert(any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void createProject_limitExceeded(Vertx vertx, VertxTestContext ctx) {
        ScenarioSetup setup = scenario().withBillingPlan("FREE", 5, 3);
        BillingPlan plan = BillingPlan.fromJson(setup.getBillingPlan());

        when(billingManager.getCurrentPlan(TEST_ORG_ID)).thenReturn(Future.succeededFuture(plan));
        when(projectRepository.countByOrganization(TEST_ORG_ID)).thenReturn(Future.succeededFuture(3L));

        JsonObject body = new JsonObject().put("name", "New Project");

        projectManager.createProject(body, TEST_USER_ID, TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("Project limit reached"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void getProject_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        JsonObject projectDoc = createTestProject(projectId, TEST_ORG_ID);

        when(projectRepository.findById(projectId)).thenReturn(Future.succeededFuture(projectDoc));

        projectManager.getProject(projectId, TEST_ORG_ID)
                .onComplete(ctx.succeeding(project -> {
                    ctx.verify(() -> {
                        assertNotNull(project);
                        assertEquals(projectId, project.getId());
                        assertEquals(TEST_ORG_ID, project.getOrganizationId());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void getProject_notFound(Vertx vertx, VertxTestContext ctx) {
        when(projectRepository.findById("nonexistent")).thenReturn(Future.succeededFuture(null));

        projectManager.getProject("nonexistent", TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.NOT_FOUND, ((AppException) err).getErrorCode());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void getProject_wrongOrganization(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        JsonObject projectDoc = createTestProject(projectId, "other-org");

        when(projectRepository.findById(projectId)).thenReturn(Future.succeededFuture(projectDoc));

        projectManager.getProject(projectId, TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.FORBIDDEN, ((AppException) err).getErrorCode());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void listProjects_success(Vertx vertx, VertxTestContext ctx) {
        List<JsonObject> docs = List.of(
                createTestProject(randomId(), TEST_ORG_ID),
                createTestProject(randomId(), TEST_ORG_ID)
        );
        when(projectRepository.findByOrganization(TEST_ORG_ID, 0, 20))
                .thenReturn(Future.succeededFuture(docs));

        projectManager.listProjects(TEST_ORG_ID, 0, 20)
                .onComplete(ctx.succeeding(projects -> {
                    ctx.verify(() -> {
                        assertEquals(2, projects.size());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void archiveProject_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        JsonObject projectDoc = createTestProject(projectId, TEST_ORG_ID);
        JsonObject archivedDoc = projectDoc.copy().put("status", "ARCHIVED");

        when(projectRepository.findById(projectId))
                .thenReturn(Future.succeededFuture(projectDoc))
                .thenReturn(Future.succeededFuture(archivedDoc));
        when(projectRepository.update(eq(projectId), any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());

        projectManager.archiveProject(projectId, TEST_ORG_ID)
                .onComplete(ctx.succeeding(project -> {
                    ctx.verify(() -> {
                        assertEquals(Project.Status.ARCHIVED, project.getStatus());
                        verify(projectRepository).update(eq(projectId), any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void archiveProject_alreadyArchived(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        JsonObject projectDoc = createTestProject(projectId, TEST_ORG_ID).put("status", "ARCHIVED");

        when(projectRepository.findById(projectId)).thenReturn(Future.succeededFuture(projectDoc));

        projectManager.archiveProject(projectId, TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.BAD_REQUEST, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("already archived"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void unarchiveProject_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        JsonObject projectDoc = createTestProject(projectId, TEST_ORG_ID).put("status", "ARCHIVED");
        JsonObject activeDoc = projectDoc.copy().put("status", "ACTIVE");

        when(projectRepository.findById(projectId))
                .thenReturn(Future.succeededFuture(projectDoc))
                .thenReturn(Future.succeededFuture(activeDoc));
        when(projectRepository.update(eq(projectId), any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());

        projectManager.unarchiveProject(projectId, TEST_ORG_ID)
                .onComplete(ctx.succeeding(project -> {
                    ctx.verify(() -> {
                        assertEquals(Project.Status.ACTIVE, project.getStatus());
                    });
                    ctx.completeNow();
                }));
    }
}
