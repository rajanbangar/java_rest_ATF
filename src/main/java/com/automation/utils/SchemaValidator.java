package com.automation.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * JSON Schema validation utilities
 */
public class SchemaValidator {
    private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);

    private SchemaValidator() {
        // Utility class
    }

    public static void validateResponse(Response response, String schemaPath) {
        try {
            response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
            logger.info("Schema validation passed for: {}", schemaPath);
        } catch (Exception e) {
            logger.error("Schema validation failed for: {}", schemaPath, e);
            throw new AssertionError("Schema validation failed: " + e.getMessage(), e);
        }
    }

    public static void validateJson(String json, String schemaPath) {
        try {
            JsonSchemaValidator validator = JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath);
            // We need to use RestAssured's internal validation
            io.restassured.RestAssured.given()
                    .body(json)
                    .then()
                    .assertThat()
                    .body(validator);
            logger.info("Schema validation passed for JSON against: {}", schemaPath);
        } catch (Exception e) {
            logger.error("Schema validation failed for JSON against: {}", schemaPath, e);
            throw new AssertionError("Schema validation failed: " + e.getMessage(), e);
        }
    }

    public static InputStream getSchemaAsStream(String schemaPath) {
        return SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
    }
}
