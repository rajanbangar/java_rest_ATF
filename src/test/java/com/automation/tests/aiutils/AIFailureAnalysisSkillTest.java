package com.automation.tests.aiutils;

import com.automation.aiUtils.skills.base.SkillContext;
import com.automation.aiUtils.skills.failureAnalysis.AIFailureAnalysisSkill;
import com.automation.aiUtils.skills.failureAnalysis.FailureAnalysis;
import com.automation.aiUtils.skills.failureAnalysis.FailureContext;
import com.automation.config.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

public class AIFailureAnalysisSkillTest {

    @Test
    public void testRuleBasedAnalysis_NotFound404() {
        AIFailureAnalysisSkill skill = new AIFailureAnalysisSkill();
        SkillContext ctx = new SkillContext(ConfigManager.getInstance());
        skill.init(ctx);

        FailureContext failureCtx = new FailureContext();
        failureCtx.setTestClass("com.example.Test");
        failureCtx.setTestMethod("testGetPost");
        failureCtx.setStatusCode(404);
        failureCtx.setErrorMessage("status code was 404 but expected 200");
        failureCtx.setTimestamp(Instant.now());

        FailureAnalysis analysis = skill.analyzeFailure(failureCtx);

        Assert.assertEquals(analysis.getCategory(), "RESOURCE_NOT_FOUND");
        Assert.assertTrue(analysis.getLikelyCause().contains("not found"));
        Assert.assertTrue(analysis.getSuggestedFix().contains("endpoint"));
        Assert.assertTrue(analysis.getConfidence() > 0.0);
        Assert.assertFalse(analysis.isAiAvailable());
    }

    @Test
    public void testRuleBasedAnalysis_Authentication401() {
        AIFailureAnalysisSkill skill = new AIFailureAnalysisSkill();
        SkillContext ctx = new SkillContext(ConfigManager.getInstance());
        skill.init(ctx);

        FailureContext failureCtx = new FailureContext();
        failureCtx.setTestClass("com.example.Test");
        failureCtx.setTestMethod("testAuth");
        failureCtx.setStatusCode(401);
        failureCtx.setErrorMessage("status code was 401 but expected 200");
        failureCtx.setTimestamp(Instant.now());

        FailureAnalysis analysis = skill.analyzeFailure(failureCtx);

        Assert.assertEquals(analysis.getCategory(), "AUTHENTICATION");
        Assert.assertTrue(analysis.getLikelyCause().contains("auth"));
        Assert.assertTrue(analysis.getConfidence() > 0.0);
        Assert.assertFalse(analysis.isAiAvailable());
    }

    @Test
    public void testRuleBasedAnalysis_ServerError500() {
        AIFailureAnalysisSkill skill = new AIFailureAnalysisSkill();
        SkillContext ctx = new SkillContext(ConfigManager.getInstance());
        skill.init(ctx);

        FailureContext failureCtx = new FailureContext();
        failureCtx.setTestClass("com.example.Test");
        failureCtx.setTestMethod("testServer");
        failureCtx.setStatusCode(500);
        failureCtx.setErrorMessage("Internal Server Error");
        failureCtx.setTimestamp(Instant.now());

        FailureAnalysis analysis = skill.analyzeFailure(failureCtx);

        Assert.assertEquals(analysis.getCategory(), "SERVER_ERROR");
        Assert.assertTrue(analysis.getLikelyCause().contains("Server error"));
        Assert.assertTrue(analysis.getConfidence() > 0.0);
        Assert.assertFalse(analysis.isAiAvailable());
    }

    @Test
    public void testRuleBasedAnalysis_AssertionFailure() {
        AIFailureAnalysisSkill skill = new AIFailureAnalysisSkill();
        SkillContext ctx = new SkillContext(ConfigManager.getInstance());
        skill.init(ctx);

        FailureContext failureCtx = new FailureContext();
        failureCtx.setTestClass("com.example.Test");
        failureCtx.setTestMethod("testAssert");
        failureCtx.setStatusCode(0);
        failureCtx.setErrorMessage("AssertionError: expected <200> but was <404>");
        failureCtx.setTimestamp(Instant.now());

        FailureAnalysis analysis = skill.analyzeFailure(failureCtx);

        Assert.assertEquals(analysis.getCategory(), "ASSERTION_FAILURE");
        Assert.assertTrue(analysis.getLikelyCause().contains("Assertion"));
        Assert.assertTrue(analysis.getConfidence() > 0.0);
        Assert.assertFalse(analysis.isAiAvailable());
    }

    @Test
    public void testRuleBasedAnalysis_Unknown() {
        AIFailureAnalysisSkill skill = new AIFailureAnalysisSkill();
        SkillContext ctx = new SkillContext(ConfigManager.getInstance());
        skill.init(ctx);

        FailureContext failureCtx = new FailureContext();
        failureCtx.setTestClass("com.example.Test");
        failureCtx.setTestMethod("testUnknown");
        failureCtx.setStatusCode(0);
        failureCtx.setErrorMessage("Some random unexpected error");
        failureCtx.setTimestamp(Instant.now());

        FailureAnalysis analysis = skill.analyzeFailure(failureCtx);

        Assert.assertEquals(analysis.getCategory(), "UNKNOWN");
        Assert.assertTrue(analysis.getConfidence() > 0.0);
        Assert.assertFalse(analysis.isAiAvailable());
    }
}
