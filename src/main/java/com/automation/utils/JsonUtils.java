package com.automation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * JSON utility methods for parsing, converting, and validating JSON
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private JsonUtils() {
        // Utility class
    }

    public static <T> T deserialize(Response response, Class<T> clazz) {
        try {
            return objectMapper.readValue(response.asString(), clazz);
        } catch (IOException e) {
            logger.error("Failed to deserialize response to {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("Failed to deserialize JSON to {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.error("Failed to serialize object", e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    public static String prettyPrint(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            logger.warn("Failed to pretty print JSON", e);
            return json;
        }
    }

    public static String prettyPrint(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("Failed to pretty print object", e);
            return object.toString();
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            logger.error("Failed to convert JSON to Map", e);
            throw new RuntimeException("Conversion failed", e);
        }
    }

    public static Map<String, Object> toMap(Response response) {
        return toMap(response.asString());
    }

    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
