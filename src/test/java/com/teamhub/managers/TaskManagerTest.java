package com.teamhub.managers;

import com.teamhub.TestBase;
import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.models.Project;
import com.teamhub.models.Task;
import com.teamhub.repositories.TaskRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
class TaskManagerTest extends TestBase {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectManager projectManager;

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new TaskManager(taskRepository, projectManager);
    }

    @Test
    void createTask_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));
        when(taskRepository.insert(any(JsonObject.class)))
                .thenReturn(Future.succeededFuture("task-123"));

        JsonObject body = new JsonObject()
                .put("title", "New Task")
                .put("projectId", projectId)
                .put("assigneeId", TEST_USER_ID);

        taskManager.createTask(body, TEST_USER_ID, TEST_ORG_ID)
                .onComplete(ctx.succeeding(task -> {
                    ctx.verify(() -> {
                        assertNotNull(task);
                        assertEquals("New Task", task.getTitle());
                        assertEquals(Task.Status.TODO, task.getStatus());
                        verify(taskRepository).insert(any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void createTask_invalidAssignee(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));

        JsonObject body = new JsonObject()
                .put("title", "New Task")
                .put("projectId", projectId)
                .put("assigneeId", "nonexistent-user");

        taskManager.createTask(body, TEST_USER_ID, TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.BAD_REQUEST, ((AppException) err).getErrorCode());
                        assertTrue(err.getMessage().contains("not a member"));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void createTask_projectNotFound(Vertx vertx, VertxTestContext ctx) {
        when(projectManager.getProject("bad-project", TEST_ORG_ID))
                .thenReturn(Future.failedFuture(new AppException(ErrorCode.NOT_FOUND, "Project not found")));

        JsonObject body = new JsonObject()
                .put("title", "New Task")
                .put("projectId", "bad-project");

        taskManager.createTask(body, TEST_USER_ID, TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.NOT_FOUND, ((AppException) err).getErrorCode());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void getTask_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        String taskId = randomId();
        JsonObject taskDoc = createTestTask(taskId, projectId);
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(taskRepository.findById(taskId)).thenReturn(Future.succeededFuture(taskDoc));
        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));

        taskManager.getTask(taskId, TEST_ORG_ID)
                .onComplete(ctx.succeeding(task -> {
                    ctx.verify(() -> {
                        assertNotNull(task);
                        assertEquals(taskId, task.getId());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void updateTask_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        String taskId = randomId();
        JsonObject taskDoc = createTestTask(taskId, projectId);
        JsonObject updatedDoc = taskDoc.copy().put("title", "Updated Title");
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(taskRepository.findById(taskId))
                .thenReturn(Future.succeededFuture(taskDoc))
                .thenReturn(Future.succeededFuture(updatedDoc));
        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));
        when(taskRepository.update(eq(taskId), any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());

        JsonObject body = new JsonObject().put("title", "Updated Title");

        taskManager.updateTask(taskId, body, TEST_ORG_ID)
                .onComplete(ctx.succeeding(task -> {
                    ctx.verify(() -> {
                        assertEquals("Updated Title", task.getTitle());
                        verify(taskRepository).update(eq(taskId), any(JsonObject.class));
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void deleteTask_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        String taskId = randomId();
        JsonObject taskDoc = createTestTask(taskId, projectId);
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(taskRepository.findById(taskId)).thenReturn(Future.succeededFuture(taskDoc));
        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));
        when(taskRepository.softDelete(taskId)).thenReturn(Future.succeededFuture());

        taskManager.deleteTask(taskId, TEST_ORG_ID)
                .onComplete(ctx.succeeding(v -> {
                    ctx.verify(() -> {
                        verify(taskRepository).softDelete(taskId);
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void updateStatus_success(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        String taskId = randomId();
        JsonObject taskDoc = createTestTask(taskId, projectId);
        JsonObject updatedDoc = taskDoc.copy().put("status", "IN_PROGRESS");
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(taskRepository.findById(taskId))
                .thenReturn(Future.succeededFuture(taskDoc))
                .thenReturn(Future.succeededFuture(updatedDoc));
        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));
        when(taskRepository.update(eq(taskId), any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());

        taskManager.updateStatus(taskId, "IN_PROGRESS", TEST_ORG_ID)
                .onComplete(ctx.succeeding(task -> {
                    ctx.verify(() -> {
                        assertEquals(Task.Status.IN_PROGRESS, task.getStatus());
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void updateStatus_invalidStatus(Vertx vertx, VertxTestContext ctx) {
        String projectId = randomId();
        String taskId = randomId();
        JsonObject taskDoc = createTestTask(taskId, projectId);
        Project project = Project.fromJson(createTestProject(projectId, TEST_ORG_ID));

        when(taskRepository.findById(taskId)).thenReturn(Future.succeededFuture(taskDoc));
        when(projectManager.getProject(projectId, TEST_ORG_ID))
                .thenReturn(Future.succeededFuture(project));

        taskManager.updateStatus(taskId, "INVALID_STATUS", TEST_ORG_ID)
                .onComplete(ctx.failing(err -> {
                    ctx.verify(() -> {
                        assertInstanceOf(AppException.class, err);
                        assertEquals(ErrorCode.VALIDATION_ERROR, ((AppException) err).getErrorCode());
                    });
                    ctx.completeNow();
                }));
    }
}
