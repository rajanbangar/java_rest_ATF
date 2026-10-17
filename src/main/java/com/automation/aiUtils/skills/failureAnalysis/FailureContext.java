package com.automation.aiUtils.skills.failureAnalysis;

import java.time.Instant;

public class FailureContext {
    private String testClass, testMethod, failureType, errorMessage, stackTrace, requestBody, responseBody;
    private int statusCode; private Instant timestamp;
    public String getTestClass(){return testClass;} public void setTestClass(String v){this.testClass=v;}
    public String getTestMethod(){return testMethod;} public void setTestMethod(String v){this.testMethod=v;}
    public String getFailureType(){return failureType;} public void setFailureType(String v){this.failureType=v;}
    public int getStatusCode(){return statusCode;} public void setStatusCode(int v){this.statusCode=v;}
    public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){this.errorMessage=v;}
    public String getStackTrace(){return stackTrace;} public void setStackTrace(String v){this.stackTrace=v;}
    public String getRequestBody(){return requestBody;} public void setRequestBody(String v){this.requestBody=v;}
    public String getResponseBody(){return responseBody;} public void setResponseBody(String v){this.responseBody=v;}
    public Instant getTimestamp(){return timestamp;} public void setTimestamp(Instant v){this.timestamp=v;}
}
