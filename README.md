# Java REST API Test Automation Framework

A TestNG + RestAssured framework for automating JSONPlaceholder APIs.

## Features

- RestAssured 5.4.0
- TestNG 7.9.0
- Jackson
- Allure Reports
- ExtentReports
- SLF4J + Logback
- Maven Profiles

## Quick Start

```bash
# Run the JSONPlaceholder suite
mvn clean test

# Run with specific environment
mvn clean test -Denvironment=prod

# Run specific group
mvn clean test -Dgroups=smoke

# Run the JSONPlaceholder post tests
mvn clean test -Dtest=JsonPlaceholderPostsTest

# Generate Allure report
mvn allure:serve
```

## Architecture Overview

This framework follows industry best practices for REST API automation with a clean, maintainable, and scalable architecture.

### Project Structure

```
java_rest_ATF/
├── pom.xml
├── testng.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── api/
│   │   │   │   ├── client/
│   │   │   │   ├── endpoints/
│   │   │   │   ├── models/
│   │   │   │   └── specs/
│   │   │   ├── config/
│   │   │   ├── listeners/
│   │   │   ├── reports/
│   │   │   └── utils/
│   │   └── resources/
│   │       ├── config.properties
│   │       └── logback.xml
│   └── test/java/com/automation/tests/jsonplaceholder/
│       └── JsonPlaceholderPostsTest.java
```

### Core Components

#### 1. Configuration Management (ConfigManager.java)
- **Singleton pattern** for thread-safe configuration access
- Loads config.properties + environment-specific overrides
- Supports system property overrides (-Dconfig.file=..., -Denvironment=...)
- Provides typed getters: getProperty(), getIntProperty(), getBooleanProperty(), getBaseUrl()

#### 2. API Client (ApiClient.java)
- **Singleton pattern** - centralized HTTP client
- Configures RestAssured with base URI, headers, timeouts, logging filters, Allure filter
- Provides fluent methods: get(), post(), put(), patch(), delete()

#### 3. Request/Response Specifications (RequestSpecs.java, ResponseSpecs.java)
- **Reusable specifications** for different scenarios
- defaultSpec(), authSpec(token), multipartSpec(), noLogSpec()
- Response specs: successSpec(), createdSpec(), noContentSpec(), badRequestSpec(), unauthorizedSpec(), notFoundSpec(), serverErrorSpec()

#### 4. JSONPlaceholder Posts Endpoint
- **Page Object / Endpoint Object pattern**
- Handles JSONPlaceholder posts through the shared API client
- Covers GET, POST, PUT, PATCH, and DELETE operations

#### 5. TestNG Listeners
- **TestListener** - Logs test start/pass/fail/skip with duration
- **AllureListener** - Enhances Allure reports with parameters, steps, attachments
- **ExtentReportListener** - Generates ExtentReports HTML reports

#### 6. Utilities
- **JsonUtils** - JSON serialization/deserialization helpers

### Configuration (config.properties)

```properties
base.url=https://jsonplaceholder.typicode.com
connection.timeout=10000
socket.timeout=10000
retry.count=3
retry.interval=2000
log.level=INFO
log.request=true
log.response=true
allure.enabled=true
extent.enabled=true
environment=qa
```

### Test Organization (testng.xml)

- **Parallel execution**: parallel="methods" with thread-count="3"
- **Groups**: smoke, regression, jsonplaceholder
- **JSONPlaceholder API Tests** - CRUD coverage for posts and retrieval of a todo
- **Listeners**: TestListener, AllureListener, ExtentReportListener

### Reporting & Logging

| Feature | Implementation |
|---------|----------------|
| **Logging** | SLF4J + Logback (Console + Rolling File Appenders) |
| **Allure Reports** | AllureListener + AllureRestAssured filter |
| **ExtentReports** | ExtentReportListener (HTML reports) |
| **TestNG Reports** | Built-in + custom TestListener |

Log files location: target/logs/application.log and target/logs/test-execution.log

### Key Design Patterns Used

| Pattern | Where Applied |
|---------|---------------|
| **Singleton** | ConfigManager, ApiClient |
| **Builder** | RequestSpecBuilder, ResponseSpecBuilder |
| **Page Object / Endpoint Object** | JsonPlaceholderEndpoints |
| **Strategy** | RequestSpecs (different specs for different scenarios)
| **Observer/Listener** | TestNG ITestListener implementations
## Dependencies (pom.xml highlights)

| Category | Libraries |
|----------|-----------|
| **API Testing** | RestAssured 5.4.0 |
| **Test Framework** | TestNG 7.9.0 |
| **JSON Processing** | Jackson 2.16.1 (databind, annotations, jsr310) |
| **Logging** | SLF4J 2.0.9 + Logback 1.4.11 |
| **Reporting** | Allure 2.24.0, ExtentReports 5.1.1 |
| **Assertions** | Hamcrest |
| **API** | JSONPlaceholder posts endpoint |

## Framework Strengths

1. **Maintainable** - Clear separation of concerns (config, client, endpoints, models, tests)
2. **Scalable** - Easy to add new endpoints, models, test suites
3. **Reusable** - Request/Response specs and endpoint wrappers
4. **Configurable** - Environment-specific configs, system property overrides
5. **Observable** - Comprehensive logging, Allure + ExtentReports
6. **Parallel-ready** - Thread-safe singletons, TestNG parallel execution
7. **API Coverage** - GET, POST, PUT, PATCH, and DELETE posts

This framework follows **industry best practices** for REST API automation and can be extended to cover additional APIs.