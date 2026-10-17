package com.automation.tests.jsonplaceholder;

import com.automation.api.endpoints.JsonPlaceholderEndpoints;
import com.automation.data.DataProviderManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;

@Epic("JSONPlaceholder")
@Feature("Posts")
public class JsonPlaceholderPostsTest {
    private final JsonPlaceholderEndpoints posts = new JsonPlaceholderEndpoints();

    @Test(groups = {"smoke", "regression", "jsonplaceholder"}, dataProviderClass = DataProviderManager.class, dataProvider = "postScenarios")
    @Description("Data-driven post tests from posts.json")
    public void runPostScenario(Map<String, Object> scenario) {
        String testName = (String) scenario.get("testName");
        String description = (String) scenario.get("description");
        int expectedStatusCode = ((Number) scenario.get("expectedStatusCode")).intValue();
        String endpoint = (String) scenario.get("endpoint");

        org.testng.Reporter.log("Running scenario: " + testName + " - " + description, true);

        switch (endpoint) {
            case "GET /posts":
                posts.getPosts()
                        .then()
                        .statusCode(expectedStatusCode)
                        .body("size()", greaterThan(0))
                        .body("[0].id", notNullValue())
                        .body("[0].title", notNullValue());
                break;

            case "GET /posts/{id}":
                @SuppressWarnings("unchecked")
                Map<String, Object> pathParams = (Map<String, Object>) scenario.get("pathParams");
                int postId = ((Number) pathParams.get("id")).intValue();
                posts.getPost(postId)
                        .then()
                        .statusCode(expectedStatusCode)
                        .body("id", equalTo(postId))
                        .body("userId", equalTo(1))
                        .body("title", notNullValue())
                        .body("body", notNullValue());
                break;

            case "POST /posts":
                @SuppressWarnings("unchecked")
                Map<String, Object> createBody = (Map<String, Object>) scenario.get("requestBody");
                posts.createPost(createBody)
                        .then()
                        .statusCode(expectedStatusCode)
                        .body("id", notNullValue())
                        .body("userId", equalTo(createBody.get("userId")))
                        .body("title", equalTo(createBody.get("title")))
                        .body("body", equalTo(createBody.get("body")));
                break;

            case "PUT /posts/{id}":
                @SuppressWarnings("unchecked")
                Map<String, Object> putPathParams = (Map<String, Object>) scenario.get("pathParams");
                int putId = ((Number) putPathParams.get("id")).intValue();
                @SuppressWarnings("unchecked")
                Map<String, Object> putBody = (Map<String, Object>) scenario.get("requestBody");
                posts.replacePost(putId, putBody)
                        .then()
                        .statusCode(expectedStatusCode)
                        .body("id", equalTo(putId))
                        .body("title", equalTo(putBody.get("title")))
                        .body("body", equalTo(putBody.get("body")));
                break;

            case "PATCH /posts/{id}":
                @SuppressWarnings("unchecked")
                Map<String, Object> patchPathParams = (Map<String, Object>) scenario.get("pathParams");
                int patchId = ((Number) patchPathParams.get("id")).intValue();
                @SuppressWarnings("unchecked")
                Map<String, Object> patchBody = (Map<String, Object>) scenario.get("requestBody");
                posts.patchPost(patchId, patchBody)
                        .then()
                        .statusCode(expectedStatusCode)
                        .body("id", equalTo(patchId))
                        .body("title", equalTo(patchBody.get("title")));
                break;

            case "DELETE /posts/{id}":
                @SuppressWarnings("unchecked")
                Map<String, Object> deletePathParams = (Map<String, Object>) scenario.get("pathParams");
                int deleteId = ((Number) deletePathParams.get("id")).intValue();
                Response response = posts.deletePost(deleteId);
                response.then().statusCode(expectedStatusCode);
                break;

            default:
                throw new IllegalArgumentException("Unknown endpoint: " + endpoint);
        }
    }
}
