package com.automation.tests.jsonplaceholder;

import com.automation.api.endpoints.JsonPlaceholderEndpoints;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@Epic("JSONPlaceholder")
@Feature("Posts")
public class JsonPlaceholderPostsTest {
    private final JsonPlaceholderEndpoints posts = new JsonPlaceholderEndpoints();

    @Test(groups = {"smoke", "regression", "jsonplaceholder"})
    @Description("Retrieve the posts collection from JSONPlaceholder")
    public void getPosts() {
        posts.getPosts()
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].title", notNullValue());
    }

    @Test(groups = {"smoke", "regression", "jsonplaceholder"})
    @Description("Retrieve a post by its identifier")
    public void getPostById() {
        posts.getPost(1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("userId", equalTo(1))
                .body("title", notNullValue())
                .body("body", notNullValue());
    }

    @Test(groups = {"regression", "jsonplaceholder"})
    @Description("Create a post and verify the returned representation")
    public void createPost() {
        Map<String, Object> post = new HashMap<>();
        post.put("userId", 1);
        post.put("title", "Automated API test");
        post.put("body", "Created by the Java REST automation framework");

        posts.createPost(post)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("userId", equalTo(1))
                .body("title", equalTo(post.get("title")))
                .body("body", equalTo(post.get("body")));
    }

    @Test(groups = {"regression", "jsonplaceholder"})
    @Description("Replace a post and verify the returned representation")
    public void replacePost() {
        Map<String, Object> post = new HashMap<>();
        post.put("id", 1);
        post.put("userId", 1);
        post.put("title", "Replaced title");
        post.put("body", "Replaced body");

        posts.replacePost(1, post)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Replaced title"))
                .body("body", equalTo("Replaced body"));
    }

    @Test(groups = {"regression", "jsonplaceholder"})
    @Description("Partially update a post and verify the changed field")
    public void patchPost() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("title", "Patched title");

        posts.patchPost(1, fields)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Patched title"));
    }

    @Test(groups = {"regression", "jsonplaceholder"})
    @Description("Delete a post")
    public void deletePost() {
        Response response = posts.deletePost(1);

        response.then().statusCode(200);
    }
}
