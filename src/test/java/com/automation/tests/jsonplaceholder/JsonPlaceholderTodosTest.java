package com.automation.tests.jsonplaceholder;

import com.automation.api.endpoints.JsonPlaceholderEndpoints;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;

@Epic("JSONPlaceholder")
@Feature("Todos")
public class JsonPlaceholderTodosTest {
    private final JsonPlaceholderEndpoints endpoints = new JsonPlaceholderEndpoints();

    @Test(groups = {"smoke", "regression", "jsonplaceholder"})
    @Description("Retrieve a todo by ID and validate its fields")
    public void getTodoById() {
        endpoints.getTodo(1)
                .then()
                .statusCode(200)
                .body("userId", equalTo(1))
                .body("id", equalTo(1))
                .body("title", not(emptyString()))
                .body("completed", equalTo(false));
    }
}
