package com.automation.tests.jsonplaceholder;

import com.automation.api.endpoints.JsonPlaceholderEndpoints;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

public class JsonPlaceholderUsersTest {
    JsonPlaceholderEndpoints users = new JsonPlaceholderEndpoints();

    @Test(groups = {"smoke", "regression", "jsonplaceholder"})
    @Description("Retrieve a todo by ID and validate its fields")
    public void getUserById(){
        users.getUser(2)
                .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("name", equalTo("Ervin Howell"))
                .body("username", equalTo("Antonette"))
                .body("email", equalTo("Shanna@melissa.tv"));
    }
}
