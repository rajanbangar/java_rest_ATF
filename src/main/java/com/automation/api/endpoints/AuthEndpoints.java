package com.automation.api.endpoints;

import com.automation.api.client.ApiClient;
import com.automation.api.models.AuthRequest;
import com.automation.api.models.AuthResponse;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auth API endpoints for reqres.in
 */
public class AuthEndpoints {
    private static final Logger logger = LoggerFactory.getLogger(AuthEndpoints.class);
    private static final String LOGIN_PATH = "/login";
    private static final String REGISTER_PATH = "/register";
    private final ApiClient apiClient;

    public AuthEndpoints() {
        this.apiClient = ApiClient.getInstance();
    }

    public Response login(AuthRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        return apiClient.post(LOGIN_PATH, request);
    }

    public Response register(AuthRequest request) {
        logger.info("Register attempt for email: {}", request.getEmail());
        return apiClient.post(REGISTER_PATH, request);
    }

    public AuthResponse loginAsObject(AuthRequest request) {
        return login(request).as(AuthResponse.class);
    }

    public AuthResponse registerAsObject(AuthRequest request) {
        return register(request).as(AuthResponse.class);
    }

    public AuthResponse loginWithValidCredentials() {
        return loginAsObject(AuthRequest.validCredentials());
    }

    public AuthResponse loginWithInvalidCredentials() {
        return loginAsObject(AuthRequest.invalidCredentials());
    }

    public AuthResponse loginWithMissingPassword() {
        return loginAsObject(AuthRequest.missingPassword());
    }
}
