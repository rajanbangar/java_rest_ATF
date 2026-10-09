package com.automation.tests.users;

import com.automation.api.models.User;
import com.automation.base.BaseTest;
import com.automation.utils.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

@Epic("User Management")
@Feature("Create User")
public class CreateUserTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "users"})
    @Description("Verify creating a new user with name and job")
    @Story("Create user with valid data")
    public void testCreateUser() {
        User newUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.createUser(newUser);
        
        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo(newUser.getName()))
                .body("job", equalTo(newUser.getJob()))
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify creating user with specific name and job")
    @Story("Create user with specific data")
    public void testCreateUserWithSpecificData() {
        String name = "John Doe";
        String job = "Software Engineer";
        User newUser = TestDataFactory.userWithNameAndJob(name, job);
        
        Response response = userEndpoints.createUser(newUser);
        
        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo(name))
                .body("job", equalTo(job))
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify created user has all required fields")
    @Story("Create user - response validation")
    public void testCreateUserResponseFields() {
        User newUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.createUser(newUser);
        User createdUser = response.as(User.class);
        
        Assert.assertNotNull(createdUser.getId());
        Assert.assertEquals(createdUser.getName(), newUser.getName());
        Assert.assertEquals(createdUser.getJob(), newUser.getJob());
        Assert.assertNotNull(createdUser.getCreatedAt());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify creating user with empty name and job")
    @Story("Create user - edge case")
    public void testCreateUserWithEmptyFields() {
        User newUser = User.builder()
                .name("")
                .job("")
                .build();
        
        Response response = userEndpoints.createUser(newUser);
        
        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo(""))
                .body("job", equalTo(""))
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify creating user with special characters")
    @Story("Create user - special characters")
    public void testCreateUserWithSpecialCharacters() {
        String name = "Jöhn Dœ!@#$%^&*()";
        String job = "Développeur & Téster";
        User newUser = TestDataFactory.userWithNameAndJob(name, job);
        
        Response response = userEndpoints.createUser(newUser);
        
        response.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo(name))
                .body("job", equalTo(job));
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify creating user as typed object")
    @Story("Create user - typed response")
    public void testCreateUserAsObject() {
        User newUser = TestDataFactory.randomUser();
        
        User createdUser = userEndpoints.createUserAsObject(newUser);
        
        Assert.assertNotNull(createdUser.getId());
        Assert.assertEquals(createdUser.getName(), newUser.getName());
        Assert.assertEquals(createdUser.getJob(), newUser.getJob());
        Assert.assertNotNull(createdUser.getCreatedAt());
    }
}
