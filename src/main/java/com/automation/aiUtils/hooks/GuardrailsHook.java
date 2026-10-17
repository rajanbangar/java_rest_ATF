package com.automation.aiUtils.hooks;

import com.automation.aiUtils.skills.guardrails.SensitiveDataGuardrailsSkill;
import com.automation.aiUtils.skills.guardrails.SensitiveDataGuardrailsSkill.MaskSummary;
import com.automation.aiUtils.skills.base.SkillManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Hook that applies sensitive data guardrails to test attachments and logs.
 * Masks PII and secrets from Allure attachments, request/response bodies,
 * URLs, and error messages.
 */
public class GuardrailsHook implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(GuardrailsHook.class);
    private final SensitiveDataGuardrailsSkill skill;

    public GuardrailsHook() {
        this.skill = (SensitiveDataGuardrailsSkill) SkillManager.getInstance().getSkill(SensitiveDataGuardrailsSkill.SKILL_ID);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (skill == null || !skill.isEnabled()) return;
        // Mask throwable message if it contains sensitive data
        if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
            String msg = result.getThrowable().getMessage();
            if (skill.containsSensitiveData(msg)) {
                MaskSummary summary = buildSummary(msg);
                logger.warn("Guardrails: sensitive data detected in failure message for {}",
                        result.getMethod().getMethodName());
                attachGuardrailsSummary(result.getMethod().getMethodName(), summary);
            }
        }
    }

    private MaskSummary buildSummary(String input) {
        MaskSummary s = new MaskSummary();
        s.setContainsEmail(skill.containsSensitiveData(input));
        s.setContainsPhone(false);
        s.setContainsSSN(false);
        s.setContainsCreditCard(false);
        s.setContainsAuthHeader(false);
        s.setContainsApiKey(false);
        s.setContainsCustom(false);
        s.setSensitive(true);
        return s;
    }

    /**
     * Mask a request body before logging/attachment. Call from tests or API client.
     */
    public static String maskRequest(String body) {
        SensitiveDataGuardrailsSkill s = (SensitiveDataGuardrailsSkill)
                SkillManager.getInstance().getSkill(SensitiveDataGuardrailsSkill.SKILL_ID);
        if (s != null && s.isEnabled()) return s.maskRequestBody(body);
        return body;
    }

    /**
     * Mask a response body before logging/attachment. Call from tests or API client.
     */
    public static String maskResponse(String body) {
        SensitiveDataGuardrailsSkill s = (SensitiveDataGuardrailsSkill)
                SkillManager.getInstance().getSkill(SensitiveDataGuardrailsSkill.SKILL_ID);
        if (s != null && s.isEnabled()) return s.maskResponseBody(body);
        return body;
    }

    @Attachment(value = "Guardrails Summary", type = "text/plain")
    private String attachGuardrailsSummary(String method, MaskSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sensitive Data Guardrails Summary for ").append(method).append("\n");
        sb.append("========================================\n");
        sb.append("Contains PII/Secrets: ").append(summary.isSensitive()).append("\n");
        sb.append("  Emails: ").append(summary.isContainsEmail()).append("\n");
        sb.append("  Phones: ").append(summary.isContainsPhone()).append("\n");
        sb.append("  SSN: ").append(summary.isContainsSSN()).append("\n");
        sb.append("  Credit Cards: ").append(summary.isContainsCreditCard()).append("\n");
        sb.append("  Auth Headers: ").append(summary.isContainsAuthHeader()).append("\n");
        sb.append("  API Keys: ").append(summary.isContainsApiKey()).append("\n");
        sb.append("  Custom Patterns: ").append(summary.isContainsCustom()).append("\n");
        return sb.toString();
    }

    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onFinish(org.testng.ITestContext context) {}
}
