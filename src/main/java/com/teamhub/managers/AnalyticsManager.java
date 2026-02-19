package com.teamhub.managers;

import com.teamhub.repositories.AnalyticsRepository;
import com.teamhub.repositories.MemberRepository;
import com.teamhub.repositories.ProjectRepository;
import com.teamhub.repositories.TaskRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsManager {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsManager.class);

    private final AnalyticsRepository analyticsRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;

    public AnalyticsManager(AnalyticsRepository analyticsRepository,
                            ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            MemberRepository memberRepository) {
        this.analyticsRepository = analyticsRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Build a dashboard summary for the organization.
     */
    public Future<JsonObject> getDashboard(String organizationId) {
        return projectRepository.countByOrganization(organizationId).compose(projectCount ->
                memberRepository.countByOrganization(organizationId).compose(memberCount ->
                        analyticsRepository.getTaskCountsByStatus(organizationId).compose(statusCounts ->
                                analyticsRepository.getTaskCountsByPriority(organizationId).compose(priorityCounts ->
                                        analyticsRepository.getRecentTaskActivity(organizationId, 10).map(recentTasks -> {
                                            long totalTasks = 0;
                                            long tasksCompleted = 0;
                                            JsonObject byStatus = new JsonObject();
                                            for (var tc : statusCounts) {
                                                String status = tc.getString("_id");
                                                long count = tc.getLong("count", 0L);
                                                byStatus.put(status, count);
                                                totalTasks += count;
                                                if ("DONE".equals(status)) tasksCompleted = count;
                                            }

                                            JsonObject byPriority = new JsonObject();
                                            for (var pc : priorityCounts) {
                                                String priority = pc.getString("_id");
                                                long count = pc.getLong("count", 0L);
                                                if (priority != null) byPriority.put(priority, count);
                                            }

                                            JsonArray activity = new JsonArray();
                                            for (var task : recentTasks) {
                                                String status = task.getString("status", "");
                                                String type = "DONE".equals(status) ? "task_completed" : "task_created";
                                                String title = task.getString("title", "Untitled");
                                                String actorId = task.getString("createdBy", "");
                                                activity.add(new JsonObject()
                                                        .put("id", task.getString("_id"))
                                                        .put("type", type)
                                                        .put("description", "DONE".equals(status)
                                                                ? "Completed task: " + title
                                                                : "Created task: " + title)
                                                        .put("actorName", actorId)
                                                        .put("createdAt", task.getString("updatedAt", "")));
                                            }

                                            return new JsonObject()
                                                    .put("totalProjects", projectCount)
                                                    .put("totalMembers", memberCount)
                                                    .put("totalTasks", totalTasks)
                                                    .put("tasksCompleted", tasksCompleted)
                                                    .put("tasksByStatus", byStatus)
                                                    .put("tasksByPriority", byPriority)
                                                    .put("recentActivity", activity);
                                        })
                                )
                        )
                )
        );
    }

    /**
     * Get task analytics for the organization.
     */
    public Future<JsonObject> getTaskAnalytics(String organizationId) {
        return analyticsRepository.getTaskCountsByStatus(organizationId).compose(taskCounts ->
                analyticsRepository.getProjectActivity(organizationId).map(projectActivity -> {
                    JsonObject result = new JsonObject()
                            .put("tasksByStatus", new JsonArray(taskCounts))
                            .put("projectActivity", new JsonArray(projectActivity));
                    return result;
                })
        );
    }

    /**
     * Get member analytics for the organization.
     */
    public Future<JsonObject> getMemberAnalytics(String organizationId) {
        return analyticsRepository.getMemberActivity(organizationId).map(memberActivity ->
                new JsonObject().put("memberActivity", new JsonArray(memberActivity))
        );
    }
}
