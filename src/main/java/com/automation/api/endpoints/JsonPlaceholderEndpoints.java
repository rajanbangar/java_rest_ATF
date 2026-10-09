package com.automation.api.endpoints;

import com.automation.api.client.ApiClient;
import io.restassured.response.Response;

import java.util.Map;

/**
 * JSONPlaceholder posts API endpoints.
 */
public class JsonPlaceholderEndpoints {
    private static final String POSTS_PATH = "/posts";
    private static final String TODOS_PATH = "/todos";
    private final ApiClient apiClient = ApiClient.getInstance();

    public Response getPosts() {
        return apiClient.get(POSTS_PATH);
    }

    public Response getPost(int postId) {
        return apiClient.get(POSTS_PATH + "/" + postId);
    }

    public Response createPost(Map<String, Object> post) {
        return apiClient.post(POSTS_PATH, post);
    }

    public Response replacePost(int postId, Map<String, Object> post) {
        return apiClient.put(POSTS_PATH + "/" + postId, post);
    }

    public Response patchPost(int postId, Map<String, Object> fields) {
        return apiClient.patch(POSTS_PATH + "/" + postId, fields);
    }

    public Response deletePost(int postId) {
        return apiClient.delete(POSTS_PATH + "/" + postId);
    }

    public Response getTodo(int todoId) {
        return apiClient.get(TODOS_PATH + "/" + todoId);
    }
}
