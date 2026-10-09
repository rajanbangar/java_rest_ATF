package com.automation.api.client;

import com.automation.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Centralized API client for making HTTP requests.
 * Configures base URI, default headers, logging, and filters.
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static ApiClient instance;
    private final RequestSpecification requestSpec;
    private final ConfigManager config;

    private ApiClient() {
        this.config = ConfigManager.getInstance();
        this.requestSpec = buildRequestSpec();
        configureRestAssured();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.requestSpecification = requestSpec;
    }

    private RequestSpecification buildRequestSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "Java-REST-ATF/1.0")
                .setConnectTimeout(config.getIntProperty("connection.timeout", 10000))
                .setSocketTimeout(config.getIntProperty("socket.timeout", 10000));

        // Add request/response logging filters if enabled
        if (config.getBooleanProperty("log.request", true)) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL));
        }
        if (config.getBooleanProperty("log.response", true)) {
            builder.addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }

        // Add Allure filter for reporting
        if (config.getBooleanProperty("allure.enabled", true)) {
            builder.addFilter(new AllureRestAssured());
        }

        return builder.build();
    }

    public Response get(String path) {
        logger.info("GET: {}", path);
        return given().spec(requestSpec).when().get(path);
    }

    public Response get(String path, Map<String, Object> queryParams) {
        logger.info("GET: {} with params: {}", path, queryParams);
        return given().spec(requestSpec).queryParams(queryParams).when().get(path);
    }

    public Response post(String path, Object body) {
        logger.info("POST: {}", path);
        return given().spec(requestSpec).body(body).when().post(path);
    }

    public Response post(String path, Object body, Map<String, Object> queryParams) {
        logger.info("POST: {} with params: {}", path, queryParams);
        return given().spec(requestSpec).queryParams(queryParams).body(body).when().post(path);
    }

    public Response put(String path, Object body) {
        logger.info("PUT: {}", path);
        return given().spec(requestSpec).body(body).when().put(path);
    }

    public Response patch(String path, Object body) {
        logger.info("PATCH: {}", path);
        return given().spec(requestSpec).body(body).when().patch(path);
    }

    public Response delete(String path) {
        logger.info("DELETE: {}", path);
        return given().spec(requestSpec).when().delete(path);
    }

    public Response delete(String path, Map<String, Object> queryParams) {
        logger.info("DELETE: {} with params: {}", path, queryParams);
        return given().spec(requestSpec).queryParams(queryParams).when().delete(path);
    }

    public RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    public void setAuthToken(String token) {
        requestSpec.header("Authorization", "Bearer " + token);
    }

    public void removeAuthToken() {
        requestSpec.removeHeader("Authorization");
    }

    public static void reset() {
        instance = null;
        RestAssured.reset();
    }
}
