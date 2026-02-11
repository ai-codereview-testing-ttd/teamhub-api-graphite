package com.teamhub;

import com.teamhub.config.AppConfig;
import com.teamhub.handlers.AnalyticsHandler;
import com.teamhub.handlers.BillingHandler;
import com.teamhub.handlers.MemberHandler;
import com.teamhub.handlers.OrganizationHandler;
import com.teamhub.handlers.ProjectHandler;
import com.teamhub.handlers.TaskHandler;
import com.teamhub.managers.AnalyticsManager;
import com.teamhub.managers.BillingManager;
import com.teamhub.managers.MemberManager;
import com.teamhub.managers.NotificationManager;
import com.teamhub.managers.OrganizationManager;
import com.teamhub.managers.ProjectManager;
import com.teamhub.managers.TaskManager;
import com.teamhub.middleware.AuthHandler;
import com.teamhub.middleware.ErrorHandler;
import com.teamhub.middleware.SecurityHeaderHandler;
import com.teamhub.repositories.AnalyticsRepository;
import com.teamhub.repositories.BillingPlanRepository;
import com.teamhub.repositories.MemberRepository;
import com.teamhub.repositories.OrganizationRepository;
import com.teamhub.repositories.ProjectRepository;
import com.teamhub.repositories.TaskRepository;
import com.teamhub.routes.ApiRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private MongoClient mongoClient;

    @Override
    public void start(Promise<Void> startPromise) {
        // Create MongoClient
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", AppConfig.MONGO_CONNECTION_STRING)
                .put("db_name", AppConfig.MONGO_DATABASE);
        mongoClient = MongoClient.createShared(vertx, mongoConfig);

        // Create repositories
        ProjectRepository projectRepository = new ProjectRepository(mongoClient);
        TaskRepository taskRepository = new TaskRepository(mongoClient);
        MemberRepository memberRepository = new MemberRepository(mongoClient);
        OrganizationRepository organizationRepository = new OrganizationRepository(mongoClient);
        BillingPlanRepository billingPlanRepository = new BillingPlanRepository(mongoClient);
        AnalyticsRepository analyticsRepository = new AnalyticsRepository(mongoClient);

        // Create managers
        BillingManager billingManager = new BillingManager(billingPlanRepository, organizationRepository, memberRepository, projectRepository);
        MemberManager memberManager = new MemberManager(memberRepository, billingManager);
        ProjectManager projectManager = new ProjectManager(projectRepository, billingManager, memberManager);
        TaskManager taskManager = new TaskManager(taskRepository, projectManager);
        OrganizationManager organizationManager = new OrganizationManager(organizationRepository);
        AnalyticsManager analyticsManager = new AnalyticsManager(analyticsRepository, projectRepository, taskRepository, memberRepository);
        NotificationManager notificationManager = new NotificationManager();

        // Create handlers
        ProjectHandler projectHandler = new ProjectHandler(projectManager);
        TaskHandler taskHandler = new TaskHandler(taskManager);
        MemberHandler memberHandler = new MemberHandler(memberManager);
        OrganizationHandler organizationHandler = new OrganizationHandler(organizationManager);
        AnalyticsHandler analyticsHandler = new AnalyticsHandler(analyticsManager);
        BillingHandler billingHandler = new BillingHandler(billingManager);

        // Create router
        Router router = Router.router(vertx);

        // Global middleware
        router.route().handler(new SecurityHeaderHandler());
        router.route().handler(CorsHandler.create()
                .addOrigin("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.PATCH)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowedHeader("Accept"));
        router.route().handler(BodyHandler.create());
        router.route().handler(new AuthHandler());

        // Error handler
        router.route().failureHandler(new ErrorHandler());

        // Health check
        router.get("/health").handler(ctx -> {
            JsonObject health = new JsonObject()
                    .put("status", "UP")
                    .put("service", "teamhub-api")
                    .put("timestamp", java.time.Instant.now().toString());
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(health.encode());
        });

        // Mount API routes
        Router apiRouter = Router.router(vertx);
        ApiRouter apiRouterSetup = new ApiRouter(projectHandler, taskHandler, memberHandler,
                organizationHandler, analyticsHandler, billingHandler);
        apiRouterSetup.mount(apiRouter);
        router.route("/api/v1/*").subRouter(apiRouter);

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(AppConfig.SERVER_PORT)
                .onSuccess(server -> {
                    logger.info("TeamHub API started on port {}", server.actualPort());
                    startPromise.complete();
                })
                .onFailure(err -> {
                    logger.error("Failed to start HTTP server", err);
                    startPromise.fail(err);
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (mongoClient != null) {
            mongoClient.close().onComplete(ar -> stopPromise.complete());
        } else {
            stopPromise.complete();
        }
    }

    public static void main(String[] args) {
        io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> logger.info("MainVerticle deployed: {}", id))
                .onFailure(err -> {
                    logger.error("Failed to deploy MainVerticle", err);
                    System.exit(1);
                });
    }
}
