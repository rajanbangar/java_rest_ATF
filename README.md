# Java REST API Test Automation Framework

A full-fledged TestNG + RestAssured framework for automating reqres.in APIs.

## Features

- RestAssured 5.4.0
- TestNG 7.9.0
- Jackson
- Lombok
- Allure Reports
- ExtentReports
- AssertJ
- Java Faker
- JSON Schema Validation
- SLF4J + Logback
- Maven Profiles
## Quick Start

```bash
# Run all tests
mvn clean test

# Run with specific environment
mvn clean test -Denvironment=prod

# Run specific group
mvn clean test -Dgroups=smoke

# Run specific test class
mvn clean test -Dtest=GetUsersTest

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
│   └── test/
│       ├── java/com/automation/
│       │   ├── base/
│       │   ├── data/
│       │   └── tests/
│       │       ├── auth/
│       │       ├── resources/
│       │       └── users/
│       └── resources/
│           └── schemas/
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
- Authentication token management: setAuthToken(), removeAuthToken()
#### 3. Request/Response Specifications (RequestSpecs.java, ResponseSpecs.java)
- **Reusable specifications** for different scenarios
- defaultSpec(), authSpec(token), multipartSpec(), noLogSpec()
- Response specs: successSpec(), createdSpec(), noContentSpec(), badRequestSpec(), unauthorizedSpec(), notFoundSpec(), serverErrorSpec()

#### 4. Endpoint Classes (UserEndpoints.java, AuthEndpoints.java, ResourceEndpoints.java)
- **Page Object / Endpoint Object pattern**
- Each class handles one resource (Users, Auth, Resources)
- Provides both raw Response and typed POJO methods
- Uses ApiClient internally for HTTP calls

#### 5. Model Classes (POJOs with Lombok)
- User, UserListResponse, AuthRequest, AuthResponse, Resource, ResourceListResponse
- Lombok: @Data, @Builder, @AllArgsConstructor, @NoArgsConstructor
- Jackson annotations for JSON serialization/deserialization

#### 6. Base Test Class (BaseTest.java)
- **Template Method pattern** for test setup/teardown
- @BeforeSuite - Initialize config, API client, endpoints
- @BeforeMethod - Reset RestAssured for clean state per test
- @AfterSuite - Cleanup
- Helper methods: setAuthToken(), clearAuthToken()

#### 7. TestNG Listeners
- **TestListener** - Logs test start/pass/fail/skip with duration
- **AllureListener** - Enhances Allure reports with parameters, steps, attachments
- **ExtentReportListener** - Generates ExtentReports HTML reports

#### 8. Utilities
- **JsonUtils** - JSON serialization/deserialization helpers
- **SchemaValidator** - JSON Schema validation against .json files
- **TestDataFactory** - Generates dynamic test data using Java Faker

### Configuration (config.properties)

```properties
base.url=https://reqres.in
api.base.path=/api
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
- **Groups**: smoke, regression, users, auth, resources
- **Three test suites**:
  1. **User API Tests** - com.automation.tests.users
  2. **Auth API Tests** - com.automation.tests.auth
  3. **Resource API Tests** - com.automation.tests.resources
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
| **Builder** | RequestSpecBuilder, ResponseSpecBuilder, Lombok @Builder |
| **Page Object / Endpoint Object** | UserEndpoints, AuthEndpoints, ResourceEndpoints |
| **Template Method** | BaseTest (@BeforeSuite, @BeforeMethod, etc.)
| **Strategy** | RequestSpecs (different specs for different scenarios)
| **Factory** | TestDataFactory (dynamic test data generation)
| **Observer/Listener** | TestNG ITestListener implementations
## Dependencies (pom.xml highlights)

| Category | Libraries |
|----------|-----------|
| **API Testing** | RestAssured 5.4.0, JSON Schema Validator |
| **Test Framework** | TestNG 7.9.0 |
| **JSON Processing** | Jackson 2.16.1 (databind, annotations, jsr310) |
| **Boilerplate Reduction** | Lombok 1.18.30 |
| **Logging** | SLF4J 2.0.9 + Logback 1.4.11 |
| **Reporting** | Allure 2.24.0, ExtentReports 5.1.1 |
| **Assertions** | AssertJ 3.24.2, Hamcrest |
| **Test Data** | Java Faker 1.0.2 |

## Example Test Flow

```java
// Test extends BaseTest -> gets configured endpoints automatically
public class GetUsersTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "users"})
    public void testGetUsersDefaultPage() {
        // Uses endpoint wrapper (clean, readable)
        Response response = userEndpoints.getUsers();

        // Hamcrest/AssertJ assertions with ResponseSpecs
        response.then()
                .spec(ResponseSpecs.successSpec())  // Reusable validation
                .body("page", equalTo(1))
                .body("data.size()", equalTo(6));
    }

    @Test(groups = {"regression", "users"})
    public void testGetUsersAsObject() {
        // Typed response using POJOs
        UserListResponse userList = userEndpoints.getUsersAsObject();
        Assert.assertEquals(userList.getPage(), 1);
        User firstUser = userList.getData().get(0);
        Assert.assertTrue(firstUser.getEmail().contains("@"));
    }
}
```

## Framework Strengths

1. **Maintainable** - Clear separation of concerns (config, client, endpoints, models, tests)
2. **Scalable** - Easy to add new endpoints, models, test suites
3. **Reusable** - Request/Response specs, endpoint wrappers, typed responses
4. **Configurable** - Environment-specific configs, system property overrides
5. **Observable** - Comprehensive logging, Allure + ExtentReports
6. **Parallel-ready** - Thread-safe singletons, TestNG parallel execution
7. **Type-safe** - POJOs with Jackson/Lombok, compile-time safety
8. **Schema Validation** - Contract testing with JSON Schema

This framework follows **industry best practices** for REST API automation and can be easily extended for any REST API, not just reqres.in.