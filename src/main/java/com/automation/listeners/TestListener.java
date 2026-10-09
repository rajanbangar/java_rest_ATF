package com.automation.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Custom TestNG listener for logging test execution events
 */
public class TestListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("=== TEST STARTED: {} ===", result.getMethod().getMethodName());
        logger.info("Test class: {}", result.getTestClass().getName());
        logger.info("Test groups: {}", result.getMethod().getGroups());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("=== TEST PASSED: {} ===", result.getMethod().getMethodName());
        logger.info("Duration: {} ms", result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("=== TEST FAILED: {} ===", result.getMethod().getMethodName());
        logger.error("Duration: {} ms", result.getEndMillis() - result.getStartMillis());
        if (result.getThrowable() != null) {
            logger.error("Failure reason: ", result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("=== TEST SKIPPED: {} ===", result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            logger.warn("Skip reason: ", result.getThrowable());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("Test failed but within success percentage: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("=== TEST SUITE STARTED: {} ===", context.getName());
        logger.info("Parallel mode: {}", context.getParallel());
        logger.info("Thread count: {}", context.getThreadCount());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("=== TEST SUITE FINISHED: {} ===", context.getName());
        logger.info("Total tests: {}", context.getAllTestMethods().length);
        logger.info("Passed: {}", context.getPassedTests().size());
        logger.info("Failed: {}", context.getFailedTests().size());
        logger.info("Skipped: {}", context.getSkippedTests().size());
    }
}
