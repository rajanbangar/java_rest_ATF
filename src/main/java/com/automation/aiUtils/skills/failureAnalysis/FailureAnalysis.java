package com.automation.aiUtils.skills.failureAnalysis;

import java.time.Instant;

public class FailureAnalysis {
    private String testClass, testMethod, source, category, likelyCause, suggestedFix, failureType, errorMessage;
    private int statusCode; private double confidence; private boolean aiAvailable; private Instant timestamp;
    public String getTestClass(){return testClass;} public void setTestClass(String v){this.testClass=v;}
    public String getTestMethod(){return testMethod;} public void setTestMethod(String v){this.testMethod=v;}
    public String getSource(){return source;} public void setSource(String v){this.source=v;}
    public String getCategory(){return category;} public void setCategory(String v){this.category=v;}
    public String getLikelyCause(){return likelyCause;} public void setLikelyCause(String v){this.likelyCause=v;}
    public String getSuggestedFix(){return suggestedFix;} public void setSuggestedFix(String v){this.suggestedFix=v;}
    public double getConfidence(){return confidence;} public void setConfidence(double v){this.confidence=v;}
    public String getFailureType(){return failureType;} public void setFailureType(String v){this.failureType=v;}
    public int getStatusCode(){return statusCode;} public void setStatusCode(int v){this.statusCode=v;}
    public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){this.errorMessage=v;}
    public boolean isAiAvailable(){return aiAvailable;} public void setAiAvailable(boolean v){this.aiAvailable=v;}
    public Instant getTimestamp(){return timestamp;} public void setTimestamp(Instant v){this.timestamp=v;}
}
