package com.automation.aiUtils.skills.base;

/**
 * Base interface for all pluggable automation skills under AIIntegration.
 * Skills are independent, configurable, and can be enabled/disabled via configuration.
 */
public interface Skill {

    String getSkillId();

    String getSkillName();

    boolean isEnabled();

    void init(SkillContext context);

    void destroy();
}
