package com.automation.aiUtils.hooks;

import com.automation.aiUtils.skills.failureAnalysis.AIFailureAnalysisSkill;
import com.automation.aiUtils.skills.failureAnalysis.FailureAnalysis;
import com.automation.aiUtils.skills.failureAnalysis.FailureContext;
import com.automation.aiUtils.skills.base.SkillManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Hook that analyzes test failures using AI (or rule-based fallback).
 * Runs on test failure to provide root cause analysis and suggested fixes.
 * Results are attached to Allure reports.
 */
public class AIFailureAnalysisHook implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(AIFailureAnalysisHook.class);
    private final AIFailureAnalysisSkill skill;

    public AIFailureAnalysisHook() {
        this.skill = (AIFailureAnalysisSkill) SkillManager.getInstance().getSkill(AIFailureAnalysisSkill.SKILL_ID);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (skill == null || !skill.isEnabled()) return;
        try {
            FailureContext ctx = new FailureContext();
            ctx.setTestClass(result.getTestClass().getName());
            ctx.setTestMethod(result.getMethod().getMethodName());
            ctx.setStatusCode(extractStatusCode(result));
            ctx.setErrorMessage(result.getThrowable() != null ? result.getThrowable().getMessage() : "unknown");
            ctx.setStackTrace(result.getThrowable() != null ? throwableToString(result.getThrowable()) : "");
            ctx.setTimestamp(java.time.Instant.now());

            FailureAnalysis analysis = skill.analyzeFailure(ctx);
            logAnalysis(analysis);
            attachAnalysis(analysis);
        } catch (Exception e) {
            logger.error("AI failure analysis hook error", e);
        }
    }

    private int extractStatusCode(ITestResult result) {
        if (result.getThrowable() != null) {
            String msg = result.getThrowable().getMessage();
            if (msg != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                        "status code was\\D*(\\d{3})").matcher(msg);
                if (m.find()) {
                    try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 0;
    }

    private String throwableToString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
        for (var e : t.getStackTrace()) sb.append("at ").append(e).append("\n");
        return sb.toString();
    }

    private void logAnalysis(FailureAnalysis a) {
        logger.warn("=== AI Failure Analysis: {} ===", a.getTestMethod());
        logger.warn("  Source: {}", a.getSource());
        logger.warn("  Category: {}", a.getCategory());
        logger.warn("  Likely Cause: {}", a.getLikelyCause());
        logger.warn("  Suggested Fix: {}", a.getSuggestedFix());
        logger.warn("  Confidence: {:.2f}", a.getConfidence());
    }

    @Attachment(value = "AI Failure Analysis", type = "text/plain")
    private String attachAnalysis(FailureAnalysis a) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI Failure Analysis for ").append(a.getTestMethod()).append("\n");
        sb.append("========================================\n");
        sb.append("Source: ").append(a.getSource()).append("\n");
        sb.append("AI Available: ").append(a.isAiAvailable()).append("\n");
        sb.append("Category: ").append(a.getCategory()).append("\n");
        sb.append("Likely Cause: ").append(a.getLikelyCause()).append("\n");
        sb.append("Suggested Fix: ").append(a.getSuggestedFix()).append("\n");
        sb.append("Confidence: ").append(String.format("%.2f", a.getConfidence())).append("\n");
        sb.append("Status Code: ").append(a.getStatusCode()).append("\n");
        sb.append("Error: ").append(a.getErrorMessage()).append("\n");
        sb.append("Timestamp: ").append(a.getTimestamp()).append("\n");
        return sb.toString();
    }

    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onFinish(org.testng.ITestContext context) {}
}
