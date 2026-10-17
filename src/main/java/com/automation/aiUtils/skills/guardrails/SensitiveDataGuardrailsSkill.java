package com.automation.aiUtils.skills.guardrails;

import com.automation.aiUtils.skills.base.Skill;
import com.automation.aiUtils.skills.base.SkillContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataGuardrailsSkill implements Skill {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveDataGuardrailsSkill.class);
    public static final String SKILL_ID = "guardrails.sensitive.data";
    public static final String SKILL_NAME = "Sensitive Data Guardrails";

    private boolean enabled = true;
    private MaskMode mode = MaskMode.MASK;
    private String maskReplacement = "****";
    private boolean maskAuthHeaders = true;
    private boolean maskEmails = true;
    private boolean maskPhones = true;
    private boolean maskCreditCards = true;
    private boolean maskSSN = true;
    private boolean maskApiKeys = true;
    private final List<Pattern> customPatterns = new ArrayList<>();

    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    private static final Pattern SSN = Pattern.compile("\\b\\d{3}-?\\d{2}-?\\d{4}\\b");
    private static final Pattern CREDIT_CARD = Pattern.compile("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b");
    private static final Pattern AUTH_HEADER = Pattern.compile("(?i)(authorization|api.key|apikey|access.token|secret)\\s*[:=]\\s*[^,\\s]+");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._-]+");
    private static final Pattern BASIC_AUTH = Pattern.compile("(?i)Basic\\s+[A-Za-z0-9+/=]+");
    private static final Pattern JSON_SECRET_KEYS = Pattern.compile("\"\\s*(password|secret|token|apiKey|api_key|accessToken|clientSecret|client_secret)\\s*\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);

    @Override public String getSkillId() { return SKILL_ID; }
    @Override public String getSkillName() { return SKILL_NAME; }
    @Override public boolean isEnabled() { return enabled; }

    @Override
    public void init(SkillContext context) {
        enabled = context.getBooleanProperty("skill.guardrails.enabled", true);
        String m = context.getProperty("skill.guardrails.mode", "MASK");
        mode = MaskMode.valueOf(m.toUpperCase());
        maskReplacement = context.getProperty("skill.guardrails.mask.replacement", "****");
        maskAuthHeaders = context.getBooleanProperty("skill.guardrails.mask.auth.headers", true);
        maskEmails = context.getBooleanProperty("skill.guardrails.mask.emails", true);
        maskPhones = context.getBooleanProperty("skill.guardrails.mask.phones", true);
        maskCreditCards = context.getBooleanProperty("skill.guardrails.mask.credit.cards", true);
        maskSSN = context.getBooleanProperty("skill.guardrails.mask.ssn", true);
        maskApiKeys = context.getBooleanProperty("skill.guardrails.mask.api.keys", true);
        String custom = context.getProperty("skill.guardrails.custom.patterns", "");
        if (custom!=null && !custom.isEmpty()) {
            for (String p : custom.split(",")) {
                p = p.trim();
                if (!p.isEmpty()) {
                    try { customPatterns.add(Pattern.compile(p)); }
                    catch (Exception e) { logger.warn("Invalid custom guardrail pattern: {}", p, e); }
                }
            }
        }
        logger.info("{} initialized (enabled={}, mode={})", SKILL_NAME, enabled, mode);
    }

    @Override public void destroy() { customPatterns.clear(); }

    public String mask(String input) {
        if (!enabled || input == null) return input;
        String result = input;
        if (maskAuthHeaders) {
            result = maskPattern(result, BEARER_TOKEN, "Bearer ");
            result = maskPattern(result, BASIC_AUTH, "Basic ");
            result = maskPattern(result, AUTH_HEADER, "$1: ");
        }
        if (maskApiKeys) {
            result = maskPattern(result, JSON_SECRET_KEYS, "$1: \"");
        }
        if (maskEmails) result = maskPattern(result, EMAIL, "");
        if (maskPhones) result = maskPattern(result, PHONE, "");
        if (maskSSN) result = maskPattern(result, SSN, "");
        if (maskCreditCards) result = maskPattern(result, CREDIT_CARD, "");
        for (Pattern custom : customPatterns) {
            result = maskPattern(result, custom, "");
        }
        return result;
    }

    public String maskRequestBody(String body) { return mask(body); }
    public String maskResponseBody(String body) { return mask(body); }
    public String maskUrl(String url) { return mask(url); }
    public String maskErrorMessage(String message) { return mask(message); }

    public boolean containsSensitiveData(String input) {
        if (!enabled || input == null) return false;
        if (maskAuthHeaders && (BEARER_TOKEN.matcher(input).find() || BASIC_AUTH.matcher(input).find() || AUTH_HEADER.matcher(input).find())) return true;
        if (maskApiKeys && JSON_SECRET_KEYS.matcher(input).find()) return true;
        if (maskEmails && EMAIL.matcher(input).find()) return true;
        if (maskPhones && PHONE.matcher(input).find()) return true;
        if (maskSSN && SSN.matcher(input).find()) return true;
        if (maskCreditCards && CREDIT_CARD.matcher(input).find()) return true;
        for (Pattern custom : customPatterns) {
            if (custom.matcher(input).find()) return true;
        }
        return false;
    }

    private String maskPattern(String input, Pattern pattern, String prefix) {
        if (mode == MaskMode.ALLOW) return input;
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement;
            if (mode == MaskMode.REDACT) {
                replacement = "";
            } else {
                String matched = matcher.group();
                if (prefix!=null && !prefix.isEmpty() && matched.startsWith(prefix)) {
                    replacement = prefix + maskReplacement;
                } else if (prefix!=null && !prefix.isEmpty() && matched.contains(":")) {
                    replacement = matched.replaceFirst(":[^:]*$", ":" + maskReplacement);
                } else {
                    replacement = maskReplacement;
                }
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public enum MaskMode { MASK, REDACT, ALLOW }

    public static class MaskSummary {
        private boolean containsEmail, containsPhone, containsSSN, containsCreditCard;
        private boolean containsAuthHeader, containsApiKey, containsCustom, sensitive;
        public boolean isContainsEmail(){return containsEmail;} public void setContainsEmail(boolean v){this.containsEmail=v;}
        public boolean isContainsPhone(){return containsPhone;} public void setContainsPhone(boolean v){this.containsPhone=v;}
        public boolean isContainsSSN(){return containsSSN;} public void setContainsSSN(boolean v){this.containsSSN=v;}
        public boolean isContainsCreditCard(){return containsCreditCard;} public void setContainsCreditCard(boolean v){this.containsCreditCard=v;}
        public boolean isContainsAuthHeader(){return containsAuthHeader;} public void setContainsAuthHeader(boolean v){this.containsAuthHeader=v;}
        public boolean isContainsApiKey(){return containsApiKey;} public void setContainsApiKey(boolean v){this.containsApiKey=v;}
        public boolean isContainsCustom(){return containsCustom;} public void setContainsCustom(boolean v){this.containsCustom=v;}
        public boolean isSensitive(){return sensitive;} public void setSensitive(boolean v){this.sensitive=v;}
    }
}
