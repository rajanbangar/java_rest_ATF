package com.automation.data;

import com.automation.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Loads JSON test data from the classpath (src/test/resources/inputData).
 * Provides typed access to test scenario definitions used by data-driven tests.
 */
public class JsonDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataLoader.class);
    private static final String INPUT_DATA_DIR = "inputData/";

    private JsonDataLoader() {
        // Utility class
    }

    /**
     * Loads a JSON file from inputData and returns it as a Map.
     */
    public static Map<String, Object> load(String fileName) {
        String path = INPUT_DATA_DIR + fileName;
        try (InputStream is = JsonDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Input data file not found: " + path);
            }
            String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return JsonUtils.toMap(json);
        } catch (Exception e) {
            logger.error("Failed to load input data: {}", path, e);
            throw new RuntimeException("Failed to load input data: " + path, e);
        }
    }

    /**
     * Loads the list of post test scenarios from posts.json.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> loadPostScenarios() {
        Map<String, Object> data = load("posts.json");
        return (List<Map<String, Object>>) data.get("posts");
    }
}