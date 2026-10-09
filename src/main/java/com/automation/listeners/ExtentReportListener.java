package com.automation.listeners;

import com.automation.config.ConfigManager;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExtentReports listener for HTML reporting
 */
public class ExtentReportListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportListener.class);
    private static final ConfigManager config = ConfigManager.getInstance();
    private static ExtentReports extent;
    private static final ConcurrentHashMap<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        if (!config.getBooleanProperty("extent.enabled", true)) {
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportPath = "target/extent-reports/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("REST API Automation Report");
        sparkReporter.config().setReportName("Reqres.in API Test Results");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", config.getProperty("environment", "qa"));
        extent.setSystemInfo("Base URL", config.getBaseUrl());
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));

        logger.info("ExtentReports initialized at: {}", new File(reportPath).getAbsolutePath());
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (extent == null) return;

        ExtentTest test = extent.createTest(result.getMethod().getMethodName())
                .assignCategory(result.getTestClass().getName());
        
        for (String group : result.getMethod().getGroups()) {
            test.assignCategory(group);
        }

        testMap.put(Thread.currentThread().getId(), test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = testMap.get(Thread.currentThread().getId());
        if (test != null) {
            test.log(Status.PASS, "Test passed");
            testMap.remove(Thread.currentThread().getId());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testMap.get(Thread.currentThread().getId());
        if (test != null) {
            test.log(Status.FAIL, result.getThrowable());
            testMap.remove(Thread.currentThread().getId());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = testMap.get(Thread.currentThread().getId());
        if (test != null) {
            test.log(Status.SKIP, result.getThrowable() != null ? result.getThrowable().getMessage() : "Test skipped");
            testMap.remove(Thread.currentThread().getId());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            logger.info("ExtentReports flushed");
        }
    }
}
