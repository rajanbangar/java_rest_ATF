package com.automation.base;

import com.automation.api.client.ApiClient;
import com.automation.api.endpoints.AuthEndpoints;
import com.automation.api.endpoints.ResourceEndpoints;
import com.automation.api.endpoints.UserEndpoints;
import com.automation.config.ConfigManager;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Base test class providing common setup and teardown for all tests
 */
public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected ConfigManager config;
    protected ApiClient apiClient;
    protected UserEndpoints userEndpoints;
    protected AuthEndpoints authEndpoints;
    protected ResourceEndpoints resourceEndpoints;

    @BeforeSuite
    public void suiteSetup() {
        logger.info("=== SUITE SETUP ===");
        config = ConfigManager.getInstance();
        ApiClient.reset();
        apiClient = ApiClient.getInstance();
        
        // Initialize endpoints
        userEndpoints = new UserEndpoints();
        authEndpoints = new AuthEndpoints();
        resourceEndpoints = new ResourceEndpoints();
        
        logger.info("Base URL: {}", config.getBaseUrl());
        logger.info("Environment: {}", config.getProperty("environment", "qa"));
    }

    @BeforeMethod
    public void testSetup() {
        logger.info("=== TEST SETUP ===");
        // Reset API client for each test to ensure clean state
        RestAssured.reset();
        apiClient = ApiClient.getInstance();
    }

    @AfterMethod
    public void testTeardown() {
        logger.info("=== TEST TEARDOWN ===");
    }

    @AfterSuite
    public void suiteTeardown() {
        logger.info("=== SUITE TEARDOWN ===");
        ApiClient.reset();
    }

    protected void setAuthToken(String token) {
        apiClient.setAuthToken(token);
    }

    protected void clearAuthToken() {
        apiClient.removeAuthToken();
    }
}
