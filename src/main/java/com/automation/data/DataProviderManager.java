package com.automation.data;

import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.Map;

/**
 * TestNG DataProvider manager that feeds test scenarios from JSON input data files.
 */
public class DataProviderManager {

    private DataProviderManager() {
        // Utility class
    }

    /**
     * DataProvider for post test scenarios loaded from posts.json.
     * Each row is a Map representing one post test scenario.
     */
    @DataProvider(name = "postScenarios")
    public static Object[] postScenarios() {
        List<Map<String, Object>> scenarios = JsonDataLoader.loadPostScenarios();
        return scenarios.toArray();
    }
}