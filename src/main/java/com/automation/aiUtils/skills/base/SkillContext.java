package com.automation.aiUtils.skills.base;

import com.automation.config.ConfigManager;

public class SkillContext {

    private final ConfigManager config;

    public SkillContext(ConfigManager config) {
        this.config = config;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public String getProperty(String key) {
        return config.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return config.getBooleanProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        return config.getIntProperty(key, defaultValue);
    }

    public double getDoubleProperty(String key, double defaultValue) {
        return config.getDoubleProperty(key, defaultValue);
    }
}
