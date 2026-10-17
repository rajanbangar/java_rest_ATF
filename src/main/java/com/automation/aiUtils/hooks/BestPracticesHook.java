package com.automation.aiUtils.hooks;

import com.automation.aiUtils.skills.bestpractices.BestPracticesSkill;
import com.automation.aiUtils.skills.base.SkillManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * Hook that enforces Automation Best Practices on test methods.
 * Runs on test start to validate test structure and annotations.
 */
public class BestPracticesHook implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(BestPracticesHook.class);
    private final BestPracticesSkill skill;

    public BestPracticesHook() {
        this.skill = (BestPracticesSkill) SkillManager.getInstance().getSkill(BestPracticesSkill.SKILL_ID);
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (skill == null || !skill.isEnabled()) return;
        try {
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            if (method != null) {
                BestPracticesSkill.ValidationResult vr = skill.validateTestMethod(
                        result.getTestClass().getRealClass(), method);
                skill.logResult(vr);
                if (vr.isFailed()) {
                    logger.error("Best practices CRITICAL violations in {} - test may be skipped",
                            result.getMethod().getMethodName());
                }
            }
        } catch (Exception e) {
            logger.warn("Best practices hook error for {}", result.getMethod().getMethodName(), e);
        }
    }

    @Override
    public void onStart(ITestContext context) {
        if (skill == null || !skill.isEnabled()) return;
        logger.info("Best Practices Hook: suite started - skill enabled={}", skill.isEnabled());
    }

    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestFailure(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onFinish(ITestContext context) {}
}
