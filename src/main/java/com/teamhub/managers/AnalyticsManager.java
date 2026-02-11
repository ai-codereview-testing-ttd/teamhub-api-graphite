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
                        analyticsRepository.getTaskCountsByStatus(organizationId).map(taskCounts -> {
                            long totalTasks = 0;
                            JsonObject statusCounts = new JsonObject();
                            for (var tc : taskCounts) {
                                String status = tc.getString("_id");
                                long count = tc.getLong("count", 0L);
                                statusCounts.put(status, count);
                                totalTasks += count;
                            }

                            return new JsonObject()
                                    .put("projects", projectCount)
                                    .put("members", memberCount)
                                    .put("totalTasks", totalTasks)
                                    .put("tasksByStatus", statusCounts);
                        })
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
