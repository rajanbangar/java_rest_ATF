package com.automation.aiUtils.skills.bestpractices;

import com.automation.aiUtils.skills.base.Skill;
import com.automation.aiUtils.skills.base.SkillContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BestPracticesSkill implements Skill {

    private static final Logger logger = LoggerFactory.getLogger(BestPracticesSkill.class);
    public static final String SKILL_ID = "automation.bestpractices";
    public static final String SKILL_NAME = "Automation Best Practices";

    private boolean enabled = true;
    private boolean failOnCritical = false;
    private boolean requireAnnotations = true;
    private boolean requireGroups = true;
    private boolean forbidHardcodedUrls = true;
    private boolean requireEndpointObjects = true;

    private static final Pattern HARDCODED_URL = Pattern.compile("https?://[\\w.-]+");
    private static final Pattern DIRECT_API_CLIENT = Pattern.compile("ApiClient\\.getInstance|new ApiClient");

    @Override public String getSkillId() { return SKILL_ID; }
    @Override public String getSkillName() { return SKILL_NAME; }
    @Override public boolean isEnabled() { return enabled; }

    @Override
    public void init(SkillContext context) {
        enabled = context.getBooleanProperty("skill.bestpractices.enabled", true);
        failOnCritical = context.getBooleanProperty("skill.bestpractices.fail.on.critical", false);
        requireAnnotations = context.getBooleanProperty("skill.bestpractices.require.annotations", true);
        requireGroups = context.getBooleanProperty("skill.bestpractices.require.groups", true);
        forbidHardcodedUrls = context.getBooleanProperty("skill.bestpractices.forbid.hardcoded.urls", true);
        requireEndpointObjects = context.getBooleanProperty("skill.bestpractices.require.endpoint.objects", true);
        logger.info("{} initialized (enabled={})", SKILL_NAME, enabled);
    }

    @Override public void destroy() {}

    public ValidationResult validateTestMethod(Class<?> testClass, java.lang.reflect.Method method) {
        ValidationResult result = new ValidationResult(testClass.getName(), method.getName());
        if (!enabled) { result.setSkipped(true); return result; }
        if (requireAnnotations) {
            if (!method.isAnnotationPresent(io.qameta.allure.Epic.class))
                result.addViolation("MISSING_EPIC", "Test method missing @Epic", ViolationLevel.WARNING);
            if (!method.isAnnotationPresent(io.qameta.allure.Feature.class))
                result.addViolation("MISSING_FEATURE", "Test method missing @Feature", ViolationLevel.WARNING);
            if (!method.isAnnotationPresent(io.qameta.allure.Description.class))
                result.addViolation("MISSING_DESCRIPTION", "Test method missing @Description", ViolationLevel.INFO);
        }
        if (requireGroups) {
            org.testng.annotations.Test t = method.getAnnotation(org.testng.annotations.Test.class);
            if (t != null && (t.groups() == null || t.groups().length == 0))
                result.addViolation("MISSING_GROUPS", "Test method missing TestNG groups", ViolationLevel.WARNING);
        }
        if (result.hasCritical() && failOnCritical) result.setFailed(true);
        return result;
    }

    public ValidationResult validateTestClass(Class<?> testClass) {
        ValidationResult result = new ValidationResult(testClass.getName(), "CLASS_LEVEL");
        if (!enabled) { result.setSkipped(true); return result; }
        if (requireAnnotations) {
            if (!testClass.isAnnotationPresent(io.qameta.allure.Epic.class))
                result.addViolation("CLASS_MISSING_EPIC", "Test class missing @Epic", ViolationLevel.WARNING);
            if (!testClass.isAnnotationPresent(io.qameta.allure.Feature.class))
                result.addViolation("CLASS_MISSING_FEATURE", "Test class missing @Feature", ViolationLevel.WARNING);
        }
        return result;
    }

    public void logResult(ValidationResult result) {
        if (result.isSkipped()) { logger.debug("Best practices skipped for {}", result.getTestMethod()); return; }
        if (result.getViolations().isEmpty()) {
            logger.info("Best practices: {} - PASSED", result.getTestMethod());
        } else {
            StringBuilder sb = new StringBuilder("Best practices violations for ").append(result.getTestMethod()).append(":\n");
            for (Violation v : result.getViolations())
                sb.append(String.format("  [%s] %s: %s%n", v.level, v.code, v.message));
            logger.warn(sb.toString());
        }
    }

    public enum ViolationLevel { INFO, WARNING, CRITICAL }
    public static class Violation { public final String code, message; public final ViolationLevel level;
        public Violation(String code, String message, ViolationLevel level) { this.code=code; this.message=message; this.level=level; } }
    public static class ValidationResult {
        private String testClass, testMethod; private boolean skipped, failed;
        private final List<Violation> violations = new ArrayList<>();
        public ValidationResult(String testClass, String testMethod) { this.testClass=testClass; this.testMethod=testMethod; }
        public String getTestClass(){return testClass;} public String getTestMethod(){return testMethod;}
        public boolean isSkipped(){return skipped;} public void setSkipped(boolean s){this.skipped=s;}
        public boolean isFailed(){return failed;} public void setFailed(boolean f){this.failed=f;}
        public List<Violation> getViolations(){return violations;}
        public void addViolation(String c, String m, ViolationLevel l){violations.add(new Violation(c,m,l));}
        public boolean hasCritical(){return violations.stream().anyMatch(v->v.level==ViolationLevel.CRITICAL);}
        public boolean hasViolations(){return !violations.isEmpty();}
    }
}
