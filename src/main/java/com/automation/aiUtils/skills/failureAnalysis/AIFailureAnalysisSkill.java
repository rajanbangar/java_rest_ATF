package com.automation.aiUtils.skills.failureAnalysis;

import com.automation.aiUtils.skills.base.Skill;
import com.automation.aiUtils.skills.base.SkillContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AIFailureAnalysisSkill implements Skill {

    private static final Logger logger = LoggerFactory.getLogger(AIFailureAnalysisSkill.class);
    public static final String SKILL_ID = "ai.failure.analysis";
    public static final String SKILL_NAME = "AI Failure Analysis";

    private boolean enabled = true;
    private String provider = "openai";
    private String apiEndpoint = "";
    private String apiKey = "";
    private String model = "gpt-4";
    private int maxTokens = 1000;
    private double temperature = 0.3;
    private int timeoutSeconds = 30;
    private boolean fallbackEnabled = true;

    private final HttpClient httpClient;
    private final ConcurrentHashMap<String, FailureAnalysis> cache = new ConcurrentHashMap<>();

    public AIFailureAnalysisSkill() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override public String getSkillId() { return SKILL_ID; }
    @Override public String getSkillName() { return SKILL_NAME; }
    @Override public boolean isEnabled() { return enabled; }

    @Override
    public void init(SkillContext context) {
        enabled = context.getBooleanProperty("skill.ai.enabled", true);
        provider = context.getProperty("skill.ai.provider", "openai");
        apiEndpoint = context.getProperty("skill.ai.api.endpoint", "");
        apiKey = context.getProperty("skill.ai.api.key", "");
        model = context.getProperty("skill.ai.model", "gpt-4");
        maxTokens = context.getIntProperty("skill.ai.max.tokens", 1000);
        temperature = context.getDoubleProperty("skill.ai.temperature", 0.3);
        timeoutSeconds = context.getIntProperty("skill.ai.analysis.timeout.seconds", 30);
        fallbackEnabled = context.getBooleanProperty("skill.ai.fallback.enabled", true);
        logger.info("{} initialized (enabled={}, provider={})", SKILL_NAME, enabled, provider);
    }

    @Override public void destroy() { cache.clear(); }

    public FailureAnalysis analyzeFailure(FailureContext ctx) {
        String key = ctx.getTestClass() + "::" + ctx.getTestMethod() + "::" + ctx.getErrorMessage();
        FailureAnalysis cached = cache.get(key);
        if (cached != null) return cached;
        FailureAnalysis analysis;
        if (enabled && isAIConfigured()) {
            try { analysis = analyzeWithAI(ctx); }
            catch (Exception e) {
                logger.warn("AI analysis failed for {}: {}", ctx.getTestMethod(), e.getMessage());
                analysis = fallbackEnabled ? analyzeWithRules(ctx) : emptyAnalysis("unavailable", e.getMessage());
            }
        } else {
            analysis = fallbackEnabled ? analyzeWithRules(ctx) : emptyAnalysis("disabled", "AI analysis disabled");
        }
        analysis.setTestClass(ctx.getTestClass());
        analysis.setTestMethod(ctx.getTestMethod());
        analysis.setFailureType(ctx.getFailureType());
        analysis.setStatusCode(ctx.getStatusCode());
        analysis.setErrorMessage(ctx.getErrorMessage());
        analysis.setTimestamp(java.time.Instant.now());
        cache.put(key, analysis);
        return analysis;
    }

    private FailureAnalysis analyzeWithAI(FailureContext ctx) throws Exception {
        String prompt = buildPrompt(ctx);
        String payload = buildPayload(prompt);
        URI uri = URI.create(apiEndpoint + "/chat/completions");
        HttpRequest req = HttpRequest.newBuilder().uri(uri)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + resolveKey(apiKey))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("AI API status " + resp.statusCode());
        FailureAnalysis a = parseAIResponse(resp.body());
        a.setSource("ai-llm"); a.setAiAvailable(true);
        return a;
    }

    private FailureAnalysis analyzeWithRules(FailureContext ctx) {
        FailureAnalysis a = new FailureAnalysis();
        String err = (ctx.getErrorMessage()!=null)?ctx.getErrorMessage().toLowerCase(): "";
        int sc = ctx.getStatusCode();
        if (sc==401||sc==403||err.contains("unauthorized")||err.contains("forbidden")) {
            a.setCategory("AUTHENTICATION"); a.setLikelyCause("Invalid or expired auth credentials");
            a.setSuggestedFix("Verify API token is valid. Check auth setup."); a.setConfidence(0.9);
        } else if (sc==404||err.contains("not found")) {
            a.setCategory("RESOURCE_NOT_FOUND"); a.setLikelyCause("Resource not found or wrong endpoint");
            a.setSuggestedFix("Verify endpoint path and resource ID."); a.setConfidence(0.85);
        } else if (sc==429||err.contains("rate limit")) {
            a.setCategory("RATE_LIMIT"); a.setLikelyCause("API rate limit exceeded");
            a.setSuggestedFix("Add retry with backoff. Reduce concurrency."); a.setConfidence(0.9);
        } else if (sc>=500 && sc<600) {
            a.setCategory("SERVER_ERROR"); a.setLikelyCause("Server error (5xx)");
            a.setSuggestedFix("Retry request. Check service health."); a.setConfidence(0.75);
        } else if (err.contains("timeout")||err.contains("connect")) {
            a.setCategory("NETWORK"); a.setLikelyCause("Network timeout or connection failure");
            a.setSuggestedFix("Increase timeout. Check network connectivity."); a.setConfidence(0.8);
        } else if (err.contains("assertion")) {
            a.setCategory("ASSERTION_FAILURE"); a.setLikelyCause("Assertion failed - value mismatch");
            a.setSuggestedFix("Review expected values. Verify API contract."); a.setConfidence(0.7);
        } else if (err.contains("json")||err.contains("parse")||err.contains("deserial")) {
            a.setCategory("SCHEMA_MISMATCH"); a.setLikelyCause("Response schema changed or JSON parse failed");
            a.setSuggestedFix("Update POJO model classes."); a.setConfidence(0.75);
        } else {
            a.setCategory("UNKNOWN"); a.setLikelyCause("Unable to categorize");
            a.setSuggestedFix("Review error and stack trace manually."); a.setConfidence(0.3);
        }
        return a;
    }

    private String buildPrompt(FailureContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze this test failure. JSON only: category, likelyCause, suggestedFix, confidence(0-1).\n");
        sb.append("Test: ").append(ctx.getTestClass()).append("#").append(ctx.getTestMethod()).append("\n");
        sb.append("Status: ").append(ctx.getStatusCode()).append("\n");
        sb.append("Error: ").append(ctx.getErrorMessage()).append("\n");
        if (ctx.getStackTrace()!=null) {
            String[] lines = ctx.getStackTrace().split("\n");
            for (int i=0; i<Math.min(15, lines.length); i++) sb.append("  ").append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    private String buildPayload(String prompt) {
        return "{\"model\":\""+model+"\",\"messages\":[{\"role\":\"system\",\"content\":\"You are a test automation failure analysis expert.\"},{\"role\":\"user\",\"content\":\""+escapeJson(prompt)+"\"}],\"max_tokens\":"+maxTokens+",\"temperature\":"+temperature+"}";
    }

    private FailureAnalysis parseAIResponse(String body) {
        FailureAnalysis a = new FailureAnalysis();
        try {
            Map<String,Object> p = com.automation.utils.JsonUtils.toMap(body);
            @SuppressWarnings("unchecked")
            java.util.List<Map<String,Object>> choices = (java.util.List<Map<String,Object>>) p.get("choices");
            if (choices!=null && !choices.isEmpty()) {
                Map<String,Object> msg = (Map<String,Object>) choices.get(0).get("message");
                String content = (String) msg.get("content");
                try {
                    Map<String,Object> aj = com.automation.utils.JsonUtils.toMap(content);
                    a.setCategory((String) aj.getOrDefault("category","UNKNOWN"));
                    a.setLikelyCause((String) aj.getOrDefault("likelyCause","No cause"));
                    a.setSuggestedFix((String) aj.getOrDefault("suggestedFix","No fix"));
                    Object c = aj.get("confidence");
                    a.setConfidence(c!=null?Double.parseDouble(c.toString()):0.5);
                } catch (Exception e) {
                    a.setCategory("AI-ANALYSIS"); a.setLikelyCause(content);
                    a.setSuggestedFix("Review AI output manually."); a.setConfidence(0.5);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse AI response", e);
            a.setCategory("PARSE_ERROR"); a.setLikelyCause("Failed to parse AI response");
            a.setSuggestedFix("Check AI response format."); a.setConfidence(0.0);
        }
        return a;
    }

    private String resolveKey(String k) {
        if (k!=null && k.startsWith("${") && k.endsWith("}")) return System.getenv(k.substring(2,k.length()-1));
        return k;
    }

    private boolean isAIConfigured() {
        return apiEndpoint!=null && !apiEndpoint.isEmpty() && apiKey!=null && !apiKey.isEmpty() && !apiKey.contains("${");
    }

    private FailureAnalysis emptyAnalysis(String src, String msg) {
        FailureAnalysis a = new FailureAnalysis();
        a.setSource(src); a.setAiAvailable(false);
        a.setLikelyCause(msg); a.setSuggestedFix("AI unavailable."); a.setConfidence(0.0);
        return a;
    }

    private String escapeJson(String s) {
        if (s==null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }
}
