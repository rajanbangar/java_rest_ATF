# Java REST API Test Automation Framework

A production-ready TestNG + RestAssured framework for automating REST APIs with layered architecture, JSON-based data-driven testing, comprehensive reporting, parallel execution, and AI-powered test failure analysis.

## Features

- **RestAssured 5.4.0** - REST API testing
- **TestNG 7.9.0** - Test framework with parallel execution
- **Jackson 2.16.1** - JSON serialization/deserialization
- **Allure 2.24.0** - Interactive test reports
- **ExtentReports 5.1.1** - HTML test reports
- **SLF4J 2.0.9 + Logback 1.4.11** - Structured logging
- **Maven Profiles** - Environment-specific configuration (qa, dev, prod)
- **Data-Driven Testing** - Test scenarios defined in JSON files and executed through TestNG data providers
- **AI-Powered Failure Analysis** - Rule-based and AI-driven test failure analysis with Allure attachments
- **Automation Best Practices Hook** - Validates test structure and annotations on test start
- **Sensitive Data Guardrails** - Masks PII and secrets from failure messages and attachments
- **Pluggable AI Skills** - ServiceLoader-based discovery of AI automation skills

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

# Run a specific test method (data-driven)
mvn clean test -Dtest=JsonPlaceholderPostsTest#runPostScenario

# Run tests and generate the Allure HTML report (allure:report is bound to the verify phase)
mvn clean verify
# Open target/site/allure-maven-plugin/index.html in a browser

# Generate and serve the Allure report interactively (opens browser)
mvn allure:serve

# View ExtentReports HTML report
# Open target/extent-reports/ExtentReport_*.html in browser
```

> **Note:** the Allure report is rendered in the `verify` phase, so it is generated only when the `test` phase succeeds. To generate a report even when tests fail, run `mvn clean verify -Dmaven.test.failure.ignore=true`. On the first run the plugin downloads Allure Commandline into `.allure/` (gitignored).

## Layer-by-Layer Architecture

The framework follows a clean, layered architecture with separation of concerns. Each layer has a distinct responsibility and communicates through well-defined interfaces.

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
│   │   │   │   ├── client/          # ApiClient.java - Centralized HTTP client
│   │   │   │   ├── endpoints/       # JsonPlaceholderEndpoints.java - API endpoint wrappers
│   │   │   │   └── specs/           # RequestSpecs.java, ResponseSpecs.java
│   │   │   ├── config/              # ConfigManager.java - Singleton configuration
│   │   │   ├── data/                # JsonDataLoader.java, DataProviderManager.java - JSON test data
│   │   │   ├── listeners/           # TestNG ITestListener implementations
│   │   │   │   ├── TestListener.java
│   │   │   │   ├── AllureListener.java
│   │   │   │   └── ExtentReportListener.java
│   │   │   ├── aiUtils/
│   │   │   │   ├── hooks/           # AI-powered TestNG hooks
│   │   │   │   │   ├── AIFailureAnalysisHook.java
│   │   │   │   │   ├── BestPracticesHook.java
│   │   │   │   │   └── GuardrailsHook.java
│   │   │   │   └── skills/          # Pluggable AI automation skills
│   │   │   │       ├── base/        # Skill, SkillContext, SkillManager
│   │   │   │       ├── failureAnalysis/  # AIFailureAnalysisSkill, FailureAnalysis, FailureContext
│   │   │   │       ├── bestpractices/    # BestPracticesSkill, ValidationResult
│   │   │   │       └── guardrails/       # SensitiveDataGuardrailsSkill, MaskSummary
│   │   │   └── utils/               # JsonUtils.java - JSON helpers
│   │   └── resources/
│   │       ├── config.properties    # Base configuration
│   │       ├── logback.xml          # Logging configuration
│   │       └── META-INF/services/
│   │           ├── com.automation.aiUtils.skills.base.Skill  # Skill ServiceLoader registration
│   │           └── org.testng.ITestNGListener               # AI hooks ServiceLoader registration
│   └── test/
│       ├── java/com/automation/tests/
│       │   ├── aiutils/
│       │   │   └── AIFailureAnalysisSkillTest.java
│       │   └── jsonplaceholder/
│       │       ├── JsonPlaceholderPostsTest.java
│       │       ├── JsonPlaceholderTodosTest.java
│       │       └── JsonPlaceholderUsersTest.java
│       └── resources/
│           └── inputData/
│               └── posts.json       # Data-driven post test scenarios
```

### Layer 1: Configuration Management (`com.automation.config`)

**Component:** `ConfigManager.java` (Singleton)

Responsible for loading configuration from properties files, supporting environment-specific overrides, and system property overrides.

**Key Features:**
- Thread-safe singleton pattern
- Loads `config.properties` plus environment-specific overrides (`config-{env}.properties`)
- System properties take precedence over file properties (`-Dkey=value`)
- Typed getters: `getProperty()`, `getIntProperty()`, `getBooleanProperty()`, `getDoubleProperty()`, `getBaseUrl()`
- Runtime reload capability

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

**Available Operations:**

| Method | Description |
|--------|-------------|
| `get(path)`, `get(path, queryParams)` | Send a GET request, optionally with query parameters |
| `post(path, body)`, `post(path, body, queryParams)` | Send a POST request with a JSON body |
| `put(path, body)` | Send a PUT request with a JSON body |
| `patch(path, body)` | Send a PATCH request with a JSON body |
| `delete(path)`, `delete(path, queryParams)` | Send a DELETE request |
| `getRequestSpec()` | Get the shared request specification |
| `setAuthToken(token)`, `removeAuthToken()` | Manage the Bearer authentication header |
| `reset()` (static) | Reset the client between test suites |

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

### Layer 4: API Endpoints (`com.automation.api.endpoints`)

**Component:** `JsonPlaceholderEndpoints.java` (Endpoint Object / Page Object Pattern)

Wraps API operations into endpoint-specific methods, hiding the HTTP details from tests.

**Key Features:**
- Encapsulates endpoint paths as constants (`/posts`, `/todos`, `/users`)
- Delegates HTTP execution to `ApiClient`
- One method per API operation
- Easy to extend for new APIs

**Available Endpoints:**

| Method | Operation |
|--------|-----------|
| `getPosts()` | GET `/posts` |
| `getPost(int postId)` | GET `/posts/{id}` |
| `createPost(Map post)` | POST `/posts` |
| `replacePost(int postId, Map post)` | PUT `/posts/{id}` |
| `patchPost(int postId, Map fields)` | PATCH `/posts/{id}` |
| `deletePost(int postId)` | DELETE `/posts/{id}` |
| `getTodo(int todoId)` | GET `/todos/{id}` |
| `getUser(int userId)` | GET `/users/{id}` |

### Layer 5: Data-Driven Testing (`com.automation.data`)

**Components:** `JsonDataLoader.java`, `DataProviderManager.java`

Loads JSON test scenarios from `src/test/resources/inputData` and feeds them to TestNG tests through data providers, keeping test data separate from test code.

**Key Features:**
- `JsonDataLoader.load(fileName)` - Loads a JSON file from `inputData/` as a `Map<String, Object>`
- `JsonDataLoader.loadPostScenarios()` - Loads the scenario list from `posts.json`
- `DataProviderManager` - TestNG `@DataProvider` (`postScenarios`) that returns each scenario as a `Map<String, Object>`
- Each scenario defines `testName`, `description`, `endpoint`, `pathParams`, `requestBody`, `expectedStatusCode`, `validations`, and `groups`
- Consumed by `JsonPlaceholderPostsTest#runPostScenario`, which dispatches on the scenario endpoint (GET, GET by id, POST, PUT, PATCH, DELETE)

### Layer 6: AI Skills Layer (`com.automation.aiUtils.skills`)

The AI skills layer provides pluggable, configurable automation skills discovered via Java `ServiceLoader`. Three skills are included:

#### Skill Interface (`com.automation.aiUtils.skills.base.Skill`)

Base interface for all pluggable skills. Each skill has a unique ID and name, an enabled flag, and `init(SkillContext)` / `destroy()` lifecycle methods.

#### SkillManager (`com.automation.aiUtils.skills.base.SkillManager`)

Singleton that discovers skills via `ServiceLoader<Skill>`, initializes them, and provides lookup by skill ID (`getSkill(id)`) plus access to all enabled skills (`getEnabledSkills()`).

#### SkillContext (`com.automation.aiUtils.skills.base.SkillContext`)

Provides configuration access to skills during initialization and execution.

#### Skill Registration (ServiceLoader)

Skills are registered in `src/main/resources/META-INF/services/com.automation.aiUtils.skills.base.Skill`:

```
com.automation.aiUtils.skills.bestpractices.BestPracticesSkill
com.automation.aiUtils.skills.failureAnalysis.AIFailureAnalysisSkill
com.automation.aiUtils.skills.guardrails.SensitiveDataGuardrailsSkill
```

#### Skill 1: AIFailureAnalysisSkill (`ai.failure.analysis`)

Analyzes test failures using OpenAI-compatible API with rule-based fallback when no API key is configured.

**Categories detected by rule-based fallback:**

| Status Code / Error Pattern | Category |
|----------------------------|----------|
| 404 | RESOURCE_NOT_FOUND |
| 401 | AUTHENTICATION |
| 500, 502, 503 | SERVER_ERROR |
| AssertionError patterns | ASSERTION_FAILURE |
| Everything else | UNKNOWN |

**Configuration (in config.properties):**

```properties
skill.ai.enabled=true
skill.ai.provider=openai
skill.ai.api.endpoint=https://api.openai.com/v1
skill.ai.api.key=${AI_API_KEY}
skill.ai.model=gpt-4
skill.ai.max.tokens=1000
skill.ai.temperature=0.3
skill.ai.analysis.timeout.seconds=30
skill.ai.fallback.enabled=true
```

When `skill.ai.api.key` is not set or `skill.ai.enabled=false`, the skill uses rule-based analysis only.

#### Skill 2: BestPracticesSkill (`automation.bestpractices`)

Validates test methods for proper annotations (`@Epic`, `@Feature`, `@Description`) and TestNG groups. Logs violations as warnings.

**Configuration:**

```properties
skill.bestpractices.enabled=true
skill.bestpractices.fail.on.critical=false
skill.bestpractices.require.annotations=true
skill.bestpractices.require.groups=true
skill.bestpractices.forbid.hardcoded.urls=true
skill.bestpractices.require.endpoint.objects=true
```

#### Skill 3: SensitiveDataGuardrailsSkill (`guardrails.sensitive.data`)

Masks sensitive data (emails, phones, SSN, credit cards, auth headers, API keys) from strings. Supports MASK, REDACT, and ALLOW modes.

**Configuration:**

```properties
skill.guardrails.enabled=true
skill.guardrails.mode=MASK
skill.guardrails.mask.replacement=****
skill.guardrails.mask.auth.headers=true
skill.guardrails.mask.emails=true
skill.guardrails.mask.phones=true
skill.guardrails.mask.credit.cards=true
skill.guardrails.mask.ssn=true
skill.guardrails.mask.api.keys=true
skill.guardrails.custom.patterns=
```

### Layer 7: AI Hooks Layer (`com.automation.aiUtils.hooks`)

Three TestNG `ITestListener` hooks integrate AI skills into the test lifecycle. They are registered via Java `ServiceLoader` in `src/main/resources/META-INF/services/org.testng.ITestNGListener`, so TestNG automatically discovers them in every runner — IntelliJ class/method runs, IntelliJ suite runs, and Maven — without needing registration in `testng.xml`.

#### AIFailureAnalysisHook

Fires on `onTestFailure`. Captures failure context (test class, method, status code, error message, stack trace), runs it through `AIFailureAnalysisSkill`, logs the analysis, and attaches it to Allure reports as "AI Failure Analysis".

#### BestPracticesHook

Fires on `onTestStart`. Validates the test method using `BestPracticesSkill` and logs any violations.

#### GuardrailsHook

Fires on `onTestFailure`. Checks failure throwable messages for sensitive data (PII, secrets) and attaches a guardrails summary to Allure reports if detected.

### Layer 8: TestNG Listeners (`com.automation.listeners`)

Three listeners implement `org.testng.ITestListener` for test lifecycle events. They are registered as listeners in `testng.xml` (the AI hooks are discovered separately via ServiceLoader, see Layer 7).

#### TestListener.java - Execution Logging

Logs test start, pass, fail, skip events with duration and failure reasons.

#### AllureListener.java - Enhanced Allure Reporting

Adds test parameters, steps, and attachments to Allure reports.

#### ExtentReportListener.java - HTML ExtentReports

Generates a standalone HTML report with test results, categories, and system info.

### Layer 9: Utilities (`com.automation.utils`)

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

### Configuration (config.properties)

#### Base Configuration (`src/main/resources/config.properties`)

```properties
# Base Configuration for API Testing
# Base URL for JSONPlaceholder
base.url=https://jsonplaceholder.typicode.com

# Timeout settings (in milliseconds)
connection.timeout=10000
socket.timeout=10000

# Retry settings
retry.count=3
retry.interval=2000

# Logging
log.level=INFO
log.request=true
log.response=true

# Reporting
allure.enabled=true
extent.enabled=true

# Environment
environment=qa

# ==================== Skill Configuration ====================
# Automation Best Practices Skill
skill.bestpractices.enabled=true
skill.bestpractices.fail.on.critical=false
skill.bestpractices.require.annotations=true
skill.bestpractices.require.groups=true
skill.bestpractices.forbid.hardcoded.urls=true
skill.bestpractices.require.endpoint.objects=true

# AI Failure Analysis Skill
skill.ai.enabled=true
skill.ai.provider=openai
skill.ai.api.endpoint=https://api.openai.com/v1
skill.ai.api.key=${AI_API_KEY}
skill.ai.model=gpt-4
skill.ai.max.tokens=1000
skill.ai.temperature=0.3
skill.ai.analysis.timeout.seconds=30
skill.ai.fallback.enabled=true

# Sensitive Data Guardrails Skill
skill.guardrails.enabled=true
skill.guardrails.mode=MASK
skill.guardrails.mask.replacement=****
skill.guardrails.mask.auth.headers=true
skill.guardrails.mask.emails=true
skill.guardrails.mask.phones=true
skill.guardrails.mask.credit.cards=true
skill.guardrails.mask.ssn=true
skill.guardrails.mask.api.keys=true
skill.guardrails.custom.patterns=
```

#### Activating Profiles

```bash
# Default profile (qa environment) - active by default
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
        <classes>
            <class name="com.automation.tests.jsonplaceholder.JsonPlaceholderUsersTest" />
            <class name="com.automation.tests.jsonplaceholder.JsonPlaceholderPostsTest" />
        </classes>
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
mvn clean test -Dtest=JsonPlaceholderPostsTest#runPostScenario

# Run with parallel execution
mvn clean test -DthreadCount=5
```

**Test Classes:**

| Class | Test Methods | Groups |
|-------|-------------|--------|
| `JsonPlaceholderPostsTest` | runPostScenario (data-driven) | smoke, regression, jsonplaceholder |
| `JsonPlaceholderTodosTest` | getTodoById | smoke, regression, jsonplaceholder |
| `JsonPlaceholderUsersTest` | getUserById | smoke, regression, jsonplaceholder |
| `AIFailureAnalysisSkillTest` | 5 rule-based analysis tests | (unit tests) |

> **Note:** `testng.xml` currently includes `JsonPlaceholderUsersTest` and `JsonPlaceholderPostsTest` through the `<classes>` section. Add further classes there (or switch back to `<packages>`) to include them in the suite; other classes can still be run individually with `-Dtest=`.
