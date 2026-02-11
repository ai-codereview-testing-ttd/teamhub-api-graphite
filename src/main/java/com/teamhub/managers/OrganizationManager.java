package com.teamhub.managers;

import com.teamhub.common.AppException;
import com.teamhub.common.ErrorCode;
import com.teamhub.models.Organization;
import com.teamhub.repositories.OrganizationRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class OrganizationManager {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationManager.class);

    private final OrganizationRepository organizationRepository;

    public OrganizationManager(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Future<Organization> createOrganization(JsonObject body, String userId) {
        String name = body.getString("name");
        String slug = generateSlug(name);

        // Check if slug already exists
        return organizationRepository.findBySlug(slug).compose(existing -> {
            if (existing != null) {
                return Future.failedFuture(new AppException(ErrorCode.CONFLICT,
                        "An organization with a similar name already exists"));
            }

            JsonObject orgDoc = new JsonObject()
                    .put("name", name)
                    .put("slug", slug)
                    .put("billingPlanId", "free")
                    .put("memberCount", 1)
                    .put("settings", new JsonObject());

            return organizationRepository.insert(orgDoc).map(id -> {
                orgDoc.put("_id", id);
                logger.info("Organization created: {} (slug: {})", id, slug);
                return Organization.fromJson(orgDoc);
            });
        });
    }

    public Future<Organization> getOrganization(String organizationId) {
        return organizationRepository.findById(organizationId).compose(doc -> {
            if (doc == null) {
                return Future.failedFuture(new AppException(ErrorCode.NOT_FOUND, "Organization not found"));
            }
            return Future.succeededFuture(Organization.fromJson(doc));
        });
    }

    public Future<Organization> updateOrganization(String organizationId, JsonObject body) {
        return getOrganization(organizationId).compose(existing -> {
            JsonObject update = new JsonObject();
            if (body.containsKey("name")) {
                update.put("name", body.getString("name"));
                update.put("slug", generateSlug(body.getString("name")));
            }

            return organizationRepository.update(organizationId, update)
                    .compose(v -> getOrganization(organizationId));
        });
    }

    public Future<Organization> updateSettings(String organizationId, JsonObject settings) {
        return getOrganization(organizationId).compose(existing -> {
            JsonObject update = new JsonObject().put("settings", settings);
            return organizationRepository.update(organizationId, update)
                    .compose(v -> getOrganization(organizationId));
        });
    }

    /**
     * Generate a URL-friendly slug from a name.
     */
    private String generateSlug(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
