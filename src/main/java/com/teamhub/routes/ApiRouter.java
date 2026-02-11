package com.teamhub.routes;

import com.teamhub.handlers.AnalyticsHandler;
import com.teamhub.handlers.BillingHandler;
import com.teamhub.handlers.MemberHandler;
import com.teamhub.handlers.OrganizationHandler;
import com.teamhub.handlers.ProjectHandler;
import com.teamhub.handlers.TaskHandler;
import io.vertx.ext.web.Router;

public class ApiRouter {

    private final ProjectHandler projectHandler;
    private final TaskHandler taskHandler;
    private final MemberHandler memberHandler;
    private final OrganizationHandler organizationHandler;
    private final AnalyticsHandler analyticsHandler;
    private final BillingHandler billingHandler;

    public ApiRouter(ProjectHandler projectHandler,
                     TaskHandler taskHandler,
                     MemberHandler memberHandler,
                     OrganizationHandler organizationHandler,
                     AnalyticsHandler analyticsHandler,
                     BillingHandler billingHandler) {
        this.projectHandler = projectHandler;
        this.taskHandler = taskHandler;
        this.memberHandler = memberHandler;
        this.organizationHandler = organizationHandler;
        this.analyticsHandler = analyticsHandler;
        this.billingHandler = billingHandler;
    }

    public void mount(Router router) {
        projectHandler.mount(router);
        taskHandler.mount(router);
        memberHandler.mount(router);
        organizationHandler.mount(router);
        analyticsHandler.mount(router);
        billingHandler.mount(router);
    }
}
