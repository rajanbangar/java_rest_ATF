package com.automation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for loading properties from config files.
 * Supports environment-specific configurations.
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        String configFile = System.getProperty("config.file", "config.properties");
        String environment = System.getProperty("environment", "qa");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                logger.warn("Config file {} not found in classpath", configFile);
                return;
            }
            properties.load(input);
            logger.info("Loaded configuration from: {}", configFile);

            // Load environment-specific overrides
            String envConfigFile = "config-" + environment + ".properties";
            try (InputStream envInput = getClass().getClassLoader().getResourceAsStream(envConfigFile)) {
                if (envInput != null) {
                    Properties envProps = new Properties();
                    envProps.load(envInput);
                    properties.putAll(envProps);
                    logger.info("Loaded environment overrides from: {}", envConfigFile);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }
    }

    public String getProperty(String key) {
        return System.getProperty(key, properties.getProperty(key));
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key {}: {}", key, value);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public String getBaseUrl() {
        return getProperty("base.url", "https://jsonplaceholder.typicode.com");
    }

    public void reload() {
        properties.clear();
        loadConfig();
    }
}
