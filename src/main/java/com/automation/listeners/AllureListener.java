package com.automation.listeners;

import com.automation.config.ConfigManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Allure listener for enhanced reporting with screenshots, logs, and attachments
 */
public class AllureListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(AllureListener.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    @Override
    public void onTestStart(ITestResult result) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            Allure.parameter("Test Method", result.getMethod().getMethodName());
            Allure.parameter("Test Class", result.getTestClass().getName());
            Allure.parameter("Groups", String.join(",", result.getMethod().getGroups()));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            Allure.step("Test passed successfully");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            if (result.getThrowable() != null) {
                attachException(result.getThrowable());
            }
            Allure.step("Test failed: " + result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            if (result.getThrowable() != null) {
                attachText("Skip Reason", result.getThrowable().getMessage());
            }
        }
    }

    @Override
    public void onStart(ITestContext context) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            Allure.parameter("Suite Name", context.getName());
            Allure.parameter("Parallel", String.valueOf(context.getSuite().getParallel()));
            Allure.parameter("Thread Count", String.valueOf(context.getSuite().getXmlSuite().getThreadCount()));
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (config.getBooleanProperty("allure.enabled", true)) {
            Allure.parameter("Total Tests", String.valueOf(context.getAllTestMethods().length));
            Allure.parameter("Passed", String.valueOf(context.getPassedTests().size()));
            Allure.parameter("Failed", String.valueOf(context.getFailedTests().size()));
            Allure.parameter("Skipped", String.valueOf(context.getSkippedTests().size()));
        }
    }

    @Attachment(value = "Exception", type = "text/plain")
    private String attachException(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        return sb.toString();
    }

    @Attachment(value = "{name}", type = "text/plain")
    private String attachText(String name, String content) {
        return content;
    }

    @Attachment(value = "Request", type = "application/json")
    public byte[] attachRequest(String request) {
        return request.getBytes(StandardCharsets.UTF_8);
    }

    @Attachment(value = "Response", type = "application/json")
    public byte[] attachResponse(String response) {
        return response.getBytes(StandardCharsets.UTF_8);
    }
}
