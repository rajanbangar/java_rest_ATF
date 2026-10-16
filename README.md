# Java REST API Test Automation Framework

A production-ready TestNG + RestAssured framework for automating REST APIs with layered architecture, comprehensive reporting, parallel execution, and self-healing AI integration support.


## Features

- **RestAssured 5.4.0** - REST API testing
- **TestNG 7.9.0** - Test framework with parallel execution
- **Jackson 2.16.1** - JSON serialization/deserialization
- **Allure 2.24.0** - Interactive test reports
- **ExtentReports 5.1.1** - HTML test reports
- **SLF4J 2.0.9 + Logback 1.4.11** - Structured logging
- **Maven Profiles** - Environment-specific configuration (dev/qa/prod)
- **Self-Healing AI Ready** - Hooks for AI-powered test failure recovery

## Quick Start

```bash
# Run the full JSONPlaceholder test suite
mvn clean test

# Run with a specific environment
mvn clean test -Denvironment=prod

# Run with a specific config file
mvn clean test -Dconfig.file=config-custom.properties

# Run specific test groups
mvn clean test -Dgroups=smoke
mvn clean test -Dgroups=regression

# Run a specific test class
mvn clean test -Dtest=JsonPlaceholderPostsTest

# Run a specific test method
mvn clean test -Dtest=JsonPlaceholderPostsTest#getPosts

# Generate and serve Allure report
mvn allure:serve

# View ExtentReports HTML report
# Open target/extent-reports/ExtentReport_*.html in browser
```

## Layer-by-Layer Architecture

The framework follows a clean, layered architecture with separation of concerns. Each layer has a distinct responsibility and communicates through well-defined interfaces.

### Project Structure

```
java_rest_ATF/
├── pom.xml
├── testng.xml
├── README.md
├── SELF_HEALING_AI_INTEGRATION_ANALYSIS.md
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── api/
│   │   │   │   ├── client/          # ApiClient.java - Centralized HTTP client
│   │   │   │   ├── endpoints/       # JsonPlaceholderEndpoints.java - API endpoint wrappers
│   │   │   │   ├── models/          # POJOs for request/response bodies
│   │   │   │   └── specs/           # RequestSpecs.java, ResponseSpecs.java
│   │   │   ├── config/              # ConfigManager.java - Singleton configuration
│   │   │   ├── listeners/           # TestNG ITestListener implementations
│   │   │   │   ├── TestListener.java
│   │   │   │   ├── AllureListener.java
│   │   │   │   └── ExtentReportListener.java
│   │   │   ├── utils/               # JsonUtils.java - JSON helpers
│   │   │   └── reports/             # Report utility classes
│   │   └── resources/
│   │       ├── config.properties    # Base configuration
│   │       ├── config-dev.properties
│   │       ├── config-prod.properties
│   │       └── logback.xml          # Logging configuration
│   └── test/
│       └── java/com/automation/tests/
│           └── jsonplaceholder/
│               ├── JsonPlaceholderPostsTest.java
│               └── JsonPlaceholderTodosTest.java
└── target/                          # Build output, logs, reports
```

### Layer 1: Configuration Management (`com.automation.config`)

**Component:** `ConfigManager.java` (Singleton)

Responsible for loading configuration from properties files, supporting environment-specific overrides, and system property overrides.

**Key Features:**
- Thread-safe singleton pattern
- Loads `config.properties` plus environment-specific overrides (`config-{env}.properties`)
- System properties take precedence over file properties (`-Dkey=value`)
- Typed getters: `getProperty()`, `getIntProperty()`, `getBooleanProperty()`, `getBaseUrl()`
- Runtime reload capability

**Code Example:**

```java
// Get singleton instance
ConfigManager config = ConfigManager.getInstance();

// Get string property with default
String baseUrl = config.getProperty("base.url", "https://jsonplaceholder.typicode.com");

// Get integer property with default
int timeout = config.getIntProperty("connection.timeout", 10000);

// Get boolean property with default
boolean logRequest = config.getBooleanProperty("log.request", true);

// Get base URL
String baseUrl = config.getBaseUrl();

// Reload configuration at runtime
config.reload();
```

**Usage in tests:**
```java
String env = config.getProperty("environment", "qa");
String baseUrl = config.getBaseUrl(); // https://jsonplaceholder.typicode.com
```

### Layer 2: API Client (`com.automation.api.client`)

**Component:** `ApiClient.java` (Singleton)

Centralized HTTP client that configures and executes all REST API requests using RestAssured.

**Key Features:**
- Singleton pattern ensuring a single RestAssured configuration
- Configurable connection and socket timeouts from `ConfigManager`
- Default JSON content type and accept headers
- Optional request/response logging filters
- Automatic Allure report integration filter
- Authentication token management (Bearer token)
- Thread-safe `RestAssured.reset()` support

**Code Example:**

```java
// Get singleton instance
ApiClient client = ApiClient.getInstance();

// GET request
Response response = client.get("/posts/1");

// GET with query parameters
Map<String, Object> params = new HashMap<>();
params.put("userId", 1);
Response response = client.get("/posts", params);

// POST request with body
Map<String, Object> post = new HashMap<>();
post.put("title", "Hello");
post.put("body", "World");
post.put("userId", 1);
Response response = client.post("/posts", post);

// PUT request
Response response = client.put("/posts/1", post);

// PATCH request
Map<String, Object> patch = new HashMap<>();
patch.put("title", "Updated");
Response response = client.patch("/posts/1", patch);

// DELETE request
Response response = client.delete("/posts/1");

// Set authentication token
client.setAuthToken("your-bearer-token");

// Remove authentication token
client.removeAuthToken();

// Reset client (useful between test suites)
ApiClient.reset();
```

**Internal Configuration:**

```java
private void configureRestAssured() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    RestAssured.requestSpecification = requestSpec;
}

private RequestSpecification buildRequestSpec() {
    RequestSpecBuilder builder = new RequestSpecBuilder()
            .setBaseUri(config.getBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addHeader("User-Agent", "Java-REST-ATF/1.0")
            .setConfig(RestAssuredConfig.config()
                    .httpClient(HttpClientConfig.httpClientConfig()

### Layer 3: Request/Response Specifications (`com.automation.api.specs`)

**Components:** `RequestSpecs.java`, `ResponseSpecs.java`

Reusable RestAssured specifications for consistent request building and response validation across tests.

**RequestSpecs.java - Available Specifications:**

| Method | Description |
|--------|-------------|
| `defaultSpec()` | Standard JSON request spec with logging, timeouts, Allure filter |
| `authSpec(String token)` | defaultSpec() + Bearer authorization header |
| `multipartSpec()` | Multipart request spec (no content type, for file uploads) |
| `noLogSpec()` | Request spec without request/response logging filters |

**Code Example:**

```java
import static com.automation.api.specs.RequestSpecs.*;
import static com.automation.api.specs.ResponseSpecs.*;

given().spec(defaultSpec()).when().get("/posts");
given().spec(authSpec("token123")).when().get("/posts/1");
given().spec(multipartSpec()).multiPart("file", file).when().post("/upload");
given().spec(noLogSpec()).when().get("/posts/1");
```

**ResponseSpecs.java - Available Specifications:**

| Method | Expected Status | Description |
|--------|-----------------|-------------|
| `successSpec()` | 200 | Successful response with JSON content type |
| `createdSpec()` | 201 | Resource created |
| `noContentSpec()` | 204 | No content (successful delete) |
| `badRequestSpec()` | 400 | Client error - bad request |
| `unauthorizedSpec()` | 401 | Authentication required |
| `notFoundSpec()` | 404 | Resource not found |
| `serverErrorSpec()` | 500/502/503 | Server errors |

**Code Example:**

```java
given().spec(defaultSpec())
    .when().get("/posts/1")

### Layer 4: API Endpoints (`com.automation.api.endpoints`)

**Component:** `JsonPlaceholderEndpoints.java` (Endpoint Object / Page Object Pattern)

Wraps API operations into endpoint-specific methods, hiding the HTTP details from tests.

**Key Features:**
- Encapsulates endpoint paths as constants
- Delegates HTTP execution to `ApiClient`
- One method per API operation
- Easy to extend for new APIs

**Code Example:**

```java
public class JsonPlaceholderEndpoints {
    private static final String POSTS_PATH = "/posts";
    private static final String TODOS_PATH = "/todos";
    private final ApiClient apiClient = ApiClient.getInstance();

    public Response getPosts() {
        return apiClient.get(POSTS_PATH);
    }

    public Response getPost(int postId) {
        return apiClient.get(POSTS_PATH + "/" + postId);
    }

    public Response createPost(Map<String, Object> post) {
        return apiClient.post(POSTS_PATH, post);
    }

    public Response replacePost(int postId, Map<String, Object> post) {
        return apiClient.put(POSTS_PATH + "/" + postId, post);
    }

    public Response patchPost(int postId, Map<String, Object> fields) {
        return apiClient.patch(POSTS_PATH + "/" + postId, fields);
    }

    public Response deletePost(int postId) {
        return apiClient.delete(POSTS_PATH + "/" + postId);
    }

    public Response getTodo(int todoId) {
        return apiClient.get(TODOS_PATH + "/" + todoId);
    }
}
```

**Usage in Tests:**

```java

### Layer 5: Data Models (`com.automation.api.models`)

POJO classes representing request and response payloads. Use Jackson annotations for JSON mapping.

**Example Model (Post.java):**

```java
package com.automation.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
```

### Layer 6: TestNG Listeners (`com.automation.listeners`)

Three listeners implement `org.testng.ITestListener` for test lifecycle events.

#### TestListener.java - Execution Logging

Logs test start, pass, fail, skip events with duration and failure reasons.

```java
public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        logger.info("=== TEST STARTED: {} ===", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("=== TEST PASSED: {} ===", result.getMethod().getMethodName());
        logger.info("Duration: {} ms", result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("=== TEST FAILED: {} ===", result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            logger.error("Failure reason: ", result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("=== TEST SKIPPED: {} ===", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("=== TEST SUITE STARTED: {} ===", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Passed: {}, Failed: {}, Skipped: {}",
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size());
    }
}
```

#### AllureListener.java - Enhanced Allure Reporting

Adds test parameters, steps, and attachments to Allure reports.

```java
public class AllureListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        Allure.parameter("Test Method", result.getMethod().getMethodName());
        Allure.parameter("Test Class", result.getTestClass().getName());
        Allure.parameter("Groups", String.join(",", result.getMethod().getGroups()));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (result.getThrowable() != null) {
            attachException(result.getThrowable());
        }
        Allure.step("Test failed: " + result.getThrowable().getMessage());

#### ExtentReportListener.java - HTML ExtentReports

Generates a standalone HTML report with test results, categories, and system info.

```java
public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static final ConcurrentHashMap<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportPath = "target/extent-reports/ExtentReport_" + timestamp + ".html";
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", config.getProperty("environment", "qa"));
        extent.setSystemInfo("Base URL", config.getBaseUrl());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testMap.get(Thread.currentThread().getId());
        if (test != null) {
            test.log(Status.FAIL, result.getThrowable());
        }
    }
}
```

**Listener Registration (testng.xml):**
```xml
<listeners>
    <listener class-name="com.automation.listeners.TestListener"/>
    <listener class-name="com.automation.listeners.AllureListener"/>
    <listener class-name="com.automation.listeners.ExtentReportListener"/>
</listeners>
```

### Layer 7: Utilities (`com.automation.utils`)

**Component:** `JsonUtils.java`

Utility methods for JSON processing.

| Method | Description |
|--------|-------------|
| `deserialize(Response, Class<T>)` | Convert RestAssured response to POJO |
| `deserialize(String, Class<T>)` | Convert JSON string to POJO |
| `serialize(Object)` | Convert POJO to JSON string |
| `prettyPrint(String/Object)` | Format JSON with indentation |
| `toMap(String/Response)` | Convert JSON to `Map<String, Object>` |
| `isValidJson(String)` | Validate if string is valid JSON |
| `getObjectMapper()` | Get the configured `ObjectMapper` |

**Code Example:**

```java
// Serialize a Post object to JSON
Post post = new Post();
post.setTitle("Hello");
String json = JsonUtils.serialize(post);

// Deserialize response to Post
Response response = client.get("/posts/1");
Post post = JsonUtils.deserialize(response, Post.class);

// Pretty print JSON
String pretty = JsonUtils.prettyPrint(json);

// Convert response to Map
Map<String, Object> map = JsonUtils.toMap(response);
Object title = map.get("title");

// Validate JSON before parsing
if (JsonUtils.isValidJson(responseString)) {
    // safe to parse
}
```

**Usage with Jackson:**

```java
// Serialize object to JSON
String json = JsonUtils.serialize(postObject);

// Deserialize JSON to object
Post post = JsonUtils.deserialize(response, Post.class);

// Deserialize response as Map
Map<String, Object> map = JsonUtils.toMap(response);

// Validate JSON
boolean isValid = JsonUtils.isValidJson(responseString);

### Configuration (config.properties)

#### Base Configuration (`src/main/resources/config.properties`)

```properties
# ==================== BASE CONFIGURATION ====================
base.url=https://jsonplaceholder.typicode.com

# ==================== TIMEOUT SETTINGS (ms) ====================
connection.timeout=10000
socket.timeout=10000

# ==================== RETRY SETTINGS ====================
retry.count=3
retry.interval=2000

# ==================== LOGGING ====================
log.level=INFO
log.request=true
log.response=true

# ==================== REPORTING ====================
allure.enabled=true
extent.enabled=true

# ==================== ENVIRONMENT ====================
environment=qa

# ==================== SELF-HEALING AI (Optional) ====================
self.healing.enabled=false
self.healing.provider=openai
self.healing.api.key=${SELF_HEALING_API_KEY}
self.healing.api.endpoint=https://api.openai.com/v1
self.healing.model=gpt-4
self.healing.max.attempts=3
self.healing.retry.interval=2000
self.healing.backoff.multiplier=2.0
self.healing.confidence.threshold=0.75

# Failure classification
self.healing.classify.network.timeout=true
self.healing.classify.server.error=true
self.healing.classify.rate.limit=true
self.healing.classify.schema.mismatch=true
self.healing.classify.flaky.assertion=true
self.healing.classify.auth.failure=true
self.healing.classify.not.found=false

# Healing strategies
self.healing.heal.endpoints=true
self.healing.heal.payloads=true
self.healing.heal.assertions=false
self.healing.learn.from.history=true
self.healing.history.days=30

# Circuit breaker
healing.circuit.breaker.threshold=5
healing.circuit.breaker.timeout=60000

# Reporting
self.healing.report.details=true
self.healing.notification.enabled=false
```

#### Environment-Specific Overrides

Create `config-dev.properties` and `config-prod.properties` in `src/main/resources/`:

**config-dev.properties:**
```properties
base.url=https://dev-api.example.com
environment=dev
log.level=DEBUG
connection.timeout=15000
socket.timeout=15000
```

**config-prod.properties:**
```properties
base.url=https://api.example.com
environment=prod
log.level=WARN
log.request=false
log.response=false
```

#### Activating Profiles

```bash
# Default (qa) profile - active by default
mvn clean test

# Dev profile
mvn clean test -Pdev

# Prod profile
mvn clean test -Pprod

# Override environment at runtime
mvn clean test -Denvironment=staging

# Override config file at runtime
mvn clean test -Dconfig.file=config-custom.properties
```

#### Configuration Access Examples

```java
ConfigManager config = ConfigManager.getInstance();

// Get base URL
String baseUrl = config.getBaseUrl();

// Get typed properties with defaults
int timeout = config.getIntProperty("connection.timeout", 10000);
boolean logRequest = config.getBooleanProperty("log.request", true);
String env = config.getProperty("environment", "qa");

// Self-healing parameters
boolean healingEnabled = config.getBooleanProperty("self.healing.enabled", false);
int maxAttempts = config.getIntProperty("self.healing.max.attempts", 3);
double confidenceThreshold = Double.parseDouble(
    config.getProperty("self.healing.confidence.threshold", "0.75"));

// Reload configuration at runtime
config.reload();
```

### Test Organization (testng.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="REST API Automation Suite" verbose="1" parallel="methods" thread-count="3">
    <listeners>
        <listener class-name="com.automation.listeners.TestListener"/>
        <listener class-name="com.automation.listeners.AllureListener"/>
        <listener class-name="com.automation.listeners.ExtentReportListener"/>
    </listeners>

    <test name="JSONPlaceholder API Tests">
        <groups>
            <run>
                <include name="smoke"/>
                <include name="regression"/>
                <include name="jsonplaceholder"/>
            </run>
        </groups>
        <packages>
            <package name="com.automation.tests.jsonplaceholder"/>
        </packages>
    </test>
</suite>
```

**Test Groups:**

| Group | Purpose |
|-------|---------|
| `smoke` | Critical path tests for fast feedback |
| `regression` | Full regression coverage |
| `jsonplaceholder` | JSONPlaceholder-specific tests |

**Running Tests by Group:**

```bash
# Run only smoke tests
mvn clean test -Dgroups=smoke

# Run smoke and regression
mvn clean test -Dgroups=smoke,regression

# Run specific test class
mvn clean test -Dtest=JsonPlaceholderPostsTest

# Run specific test method
mvn clean test -Dtest=JsonPlaceholderPostsTest#getPosts

# Run with parallel execution
mvn clean test -DthreadCount=5
```

**Test Classes:**

| Class | Test Methods | Groups |
|-------|-------------|--------|
| `JsonPlaceholderPostsTest` | getPosts, getPostById, createPost, replacePost, patchPost, deletePost | smoke, regression, jsonplaceholder |
| `JsonPlaceholderTodosTest` | getTodoById | smoke, regression, jsonplaceholder |

### Reporting & Logging

| Feature | Implementation | Output Location |
|---------|---------------|-----------------|
| **Logging** | SLF4J + Logback (Console + Rolling File Appenders) | `target/logs/application.log`, `target/logs/test-execution.log` |
| **Allure Reports** | AllureListener + AllureRestAssured filter | `target/allure-results/` → `mvn allure:serve` |
| **ExtentReports** | ExtentReportListener (HTML reports) | `target/extent-reports/ExtentReport_*.html` |
| **TestNG Reports** | Built-in + custom TestListener | `target/surefire-reports/` |

**Log Files Location:**
```
target/logs/application.log
target/logs/test-execution.log
target/logs/application-2026-01-15.1.log
target/logs/test-execution-2026-01-15.1.log
```

**Generating Reports:**

```bash
# Allure report (interactive web UI)
mvn allure:serve

# Allure report (static HTML)
mvn allure:report
# Open target/site/allure-maven-plugin/index.html

# ExtentReports (HTML)
# Open target/extent-reports/ExtentReport_*.html in browser
```

**Logback Configuration (`logback.xml`):**

| Appender | Destination | Rolling Policy |
|----------|-------------|----------------|
| CONSOLE | stdout | N/A |
| FILE | `target/logs/application.log` | Time-based, 10MB max, 30 days, 100MB cap |
| TEST_FILE | `target/logs/test-execution.log` | Time-based, 10MB max, 30 days, 100MB cap |

**Log Levels:**
- `com.automation` → DEBUG
- `io.restassured` → INFO
- `org.apache.http` → WARN
- Root → INFO

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

## Extending the Framework

### 1. Adding a New API Endpoint

```java
// Step 1: Create endpoint class
public class AlbumEndpoints {
    private static final String ALBUMS_PATH = "/albums";
    private final ApiClient apiClient = ApiClient.getInstance();

    public Response getAlbums() {
        return apiClient.get(ALBUMS_PATH);
    }

    public Response getAlbum(int albumId) {
        return apiClient.get(ALBUMS_PATH + "/" + albumId);
    }

    public Response createAlbum(Map<String, Object> album) {
        return apiClient.post(ALBUMS_PATH, album);
    }
}
```

### 2. Adding a New Test Class

```java
@Epic("JSONPlaceholder")
@Feature("Albums")
public class JsonPlaceholderAlbumsTest {
    private final AlbumEndpoints albums = new AlbumEndpoints();

    @Test(groups = {"regression", "jsonplaceholder"})
    @Description("Retrieve all albums")
    public void getAlbums() {
        albums.getAlbums()
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}
```

### 3. Adding a New Environment Profile

1. Create `config-{env}.properties` in `src/main/resources/`
2. Add profile to `pom.xml` `<profiles>` section
3. Run with `-P{env}`

### 4. Adding Custom Request Spec

```java
public static RequestSpecification customSpec(String token) {
    return defaultSpec()
        .header("Authorization", "Bearer " + token)
        .header("X-Custom-Header", "value");
}
```

---

## Best Practices

1. **Always use `ApiClient` singleton** - Don't create RestAssured requests directly in tests
2. **Use Endpoint Objects** - Encapsulate API paths in endpoint classes, not in test methods
3. **Use ResponseSpecs** - Centralize status code and content type validation
4. **Use TestNG groups** - Organize tests by smoke/regression for selective execution
5. **Keep tests independent** - Don't share state between test methods
6. **Use meaningful Allure annotations** - `@Epic`, `@Feature`, `@Description` for better reports
7. **Externalize configuration** - Never hardcode URLs, timeouts, or credentials in Java code
8. **Use system properties for overrides** - `-Denvironment=prod` for runtime flexibility
9. **Handle authentication via `setAuthToken()`** - Centralized token management
10. **Reset client between suites** - `ApiClient.reset()` when switching environments

---

## Self-Healing AI Integration

The framework is designed to support self-healing AI integration. See [`SELF_HEALING_AI_INTEGRATION_ANALYSIS.md`](SELF_HEALING_AI_INTEGRATION_ANALYSIS.md) for the complete analysis, parameters, and implementation plan.

**Key Integration Points:**

| Component | Integration Point | AI Action |
|-----------|------------------|-----------|
| `ApiClient.java` | `get()`, `post()`, `put()`, `patch()`, `delete()` | Intercept HTTP errors, apply healing strategies, retry with modifications |
| `ConfigManager.java` | `getInstance()`, `getProperty()` | Load AI provider config, healing thresholds, feature flags |
| `TestListener.java` | `onTestFailure()` | Capture failure context, trigger healing workflow |
| `AllureListener.java` | `onTestFailure()` | Attach healing attempt data to Allure reports |
| `JsonPlaceholderEndpoints.java` | All HTTP method wrappers | Dynamic endpoint path healing, fallback endpoints |
| `ResponseSpecs.java` | All `expectStatusCode()` | Dynamic status code acceptance based on AI prediction |
| `JsonUtils.java` | `deserialize()` | Schema adaptation, field mapping healing |

**Example `@SelfHealing` annotation (planned):**

```java
@SelfHealing(maxAttempts = 3, retryInterval = 2000,
    strategies = {HealingStrategy.RETRY_WITH_BACKOFF, HealingStrategy.ENDPOINT_HEALING})
@Test(groups = {"regression"})
public void getPosts() {
    posts.getPosts().then().statusCode(200);
}
```

**Self-healing parameters (add to config.properties):**
```properties
self.healing.enabled=true
self.healing.provider=openai
self.healing.max.attempts=3
self.healing.confidence.threshold=0.75
self.healing.heal.endpoints=true
self.healing.heal.payloads=true
self.healing.learn.from.history=true
```

---

## Framework Strengths

1. **Maintainable** - Clear separation of concerns (config, client, endpoints, models, tests)
2. **Scalable** - Easy to add new endpoints, models, test suites
3. **Reusable** - Request/Response specs and endpoint wrappers
4. **Configurable** - Environment-specific configs, system property overrides
5. **Observable** - Comprehensive logging, Allure + ExtentReports
6. **Parallel-ready** - Thread-safe singletons, TestNG parallel execution
7. **API Coverage** - GET, POST, PUT, PATCH, and DELETE posts
8. **AI-Ready** - Self-healing hooks and parameters for automated test recovery

---

This framework follows **industry best practices** for REST API automation and is designed to be maintainable, scalable, and extensible for additional APIs and self-healing AI capabilities.