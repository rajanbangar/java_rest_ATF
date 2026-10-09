package com.automation.api.endpoints;

import com.automation.api.client.ApiClient;
import com.automation.api.models.Resource;
import com.automation.api.models.ResourceListResponse;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Resource API endpoints for reqres.in
 */
public class ResourceEndpoints {
    private static final Logger logger = LoggerFactory.getLogger(ResourceEndpoints.class);
    private static final String RESOURCES_PATH = "/unknown";
    private final ApiClient apiClient;

    public ResourceEndpoints() {
        this.apiClient = ApiClient.getInstance();
    }

    public Response getResources(int page) {
        logger.info("Getting resources list, page: {}", page);
        return apiClient.get(RESOURCES_PATH, Map.of("page", page));
    }

    public Response getResources() {
        return getResources(1);
    }

    public Response getResource(int resourceId) {
        logger.info("Getting resource with id: {}", resourceId);
        return apiClient.get(RESOURCES_PATH + "/" + resourceId);
    }

    public Response getResourceWithQueryParams(Map<String, Object> queryParams) {
        logger.info("Getting resources with query params: {}", queryParams);
        return apiClient.get(RESOURCES_PATH, queryParams);
    }

    // Helper methods to get typed responses
    public ResourceListResponse getResourcesAsObject(int page) {
        return getResources(page).as(ResourceListResponse.class);
    }

    public ResourceListResponse getResourcesAsObject() {
        return getResourcesAsObject(1);
    }

    public Resource getResourceAsObject(int resourceId) {
        return getResource(resourceId).as(Resource.class);
    }
}
