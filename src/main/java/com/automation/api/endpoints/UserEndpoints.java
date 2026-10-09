package com.automation.api.endpoints;

import com.automation.api.client.ApiClient;
import com.automation.api.models.User;
import com.automation.api.models.UserListResponse;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * User API endpoints for reqres.in
 */
public class UserEndpoints {
    private static final Logger logger = LoggerFactory.getLogger(UserEndpoints.class);
    private static final String USERS_PATH = "/users";
    private final ApiClient apiClient;

    public UserEndpoints() {
        this.apiClient = ApiClient.getInstance();
    }

    public Response getUsers(int page) {
        logger.info("Getting users list, page: {}", page);
        return apiClient.get(USERS_PATH, Map.of("page", page));
    }

    public Response getUsers() {
        return getUsers(1);
    }

    public Response getUser(int userId) {
        logger.info("Getting user with id: {}", userId);
        return apiClient.get(USERS_PATH + "/" + userId);
    }

    public Response createUser(User user) {
        logger.info("Creating user: {}", user);
        return apiClient.post(USERS_PATH, user);
    }

    public Response updateUser(int userId, User user) {
        logger.info("Updating user id: {} with data: {}", userId, user);
        return apiClient.put(USERS_PATH + "/" + userId, user);
    }

    public Response patchUser(int userId, User user) {
        logger.info("Patching user id: {} with data: {}", userId, user);
        return apiClient.patch(USERS_PATH + "/" + userId, user);
    }

    public Response deleteUser(int userId) {
        logger.info("Deleting user with id: {}", userId);
        return apiClient.delete(USERS_PATH + "/" + userId);
    }

    public Response getUserWithQueryParams(Map<String, Object> queryParams) {
        logger.info("Getting users with query params: {}", queryParams);
        return apiClient.get(USERS_PATH, queryParams);
    }

    // Helper methods to get typed responses
    public UserListResponse getUsersAsObject(int page) {
        return getUsers(page).as(UserListResponse.class);
    }

    public UserListResponse getUsersAsObject() {
        return getUsersAsObject(1);
    }

    public User getUserAsObject(int userId) {
        return getUser(userId).as(User.class);
    }

    public User createUserAsObject(User user) {
        return createUser(user).as(User.class);
    }

    public User updateUserAsObject(int userId, User user) {
        return updateUser(userId, user).as(User.class);
    }

    public User patchUserAsObject(int userId, User user) {
        return patchUser(userId, user).as(User.class);
    }
}
