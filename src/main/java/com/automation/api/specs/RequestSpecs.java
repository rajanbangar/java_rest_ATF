package com.automation.api.specs;

import com.automation.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.restassured.AllureRestAssured;

/**
 * Reusable request specifications for different scenarios
 */
public class RequestSpecs {
    private static final ConfigManager config = ConfigManager.getInstance();

    public static RequestSpecification defaultSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "Java-REST-ATF/1.0")
                .setConnectTimeout(config.getIntProperty("connection.timeout", 10000))
                .setSocketTimeout(config.getIntProperty("socket.timeout", 10000));

        if (config.getBooleanProperty("log.request", true)) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL));
        }
        if (config.getBooleanProperty("log.response", true)) {
            builder.addFilter(new ResponseLoggingFilter(LogDetail.ALL));
        }
        if (config.getBooleanProperty("allure.enabled", true)) {
            builder.addFilter(new AllureRestAssured());
        }

        return builder.build();
    }

    public static RequestSpecification authSpec(String token) {
        return defaultSpec().header("Authorization", "Bearer " + token);
    }

    public static RequestSpecification multipartSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "Java-REST-ATF/1.0")
                .setConnectTimeout(config.getIntProperty("connection.timeout", 10000))
                .setSocketTimeout(config.getIntProperty("socket.timeout", 10000))
                .addFilter(new AllureRestAssured())
                .build();
    }

    public static RequestSpecification noLogSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "Java-REST-ATF/1.0")
                .setConnectTimeout(config.getIntProperty("connection.timeout", 10000))
                .setSocketTimeout(config.getIntProperty("socket.timeout", 10000))
                .build();
    }
}
