package com.automation.tests.users;

import com.automation.api.models.User;
import com.automation.api.models.UserListResponse;
import com.automation.base.BaseTest;
import com.automation.utils.SchemaValidator;
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
@Feature("Get Users")
public class GetUsersTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "users"})
    @Description("Verify getting list of users with default page")
    @Story("Get users list - default page")
    public void testGetUsersDefaultPage() {
        Response response = userEndpoints.getUsers();
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("page", equalTo(1))
                .body("per_page", equalTo(6))
                .body("total", greaterThan(0))
                .body("total_pages", greaterThan(0))
                .body("data.size()", equalTo(6))
                .body("data[0].id", notNullValue())
                .body("data[0].email", containsString("@"))
                .body("data[0].first_name", notNullValue())
                .body("data[0].last_name", notNullValue())
                .body("data[0].avatar", notNullValue())
                .body("support.url", notNullValue())
                .body("support.text", notNullValue());
    }

    @Test(groups = {"smoke", "regression", "users"})
    @Description("Verify getting list of users with specific page")
    @Story("Get users list - specific page")
    public void testGetUsersSpecificPage() {
        int page = 2;
        Response response = userEndpoints.getUsers(page);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("page", equalTo(page))
                .body("per_page", equalTo(6))
                .body("data.size()", equalTo(6));
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify getting single user by ID")
    @Story("Get single user")
    public void testGetSingleUser() {
        int userId = 2;
        Response response = userEndpoints.getUser(userId);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.id", equalTo(userId))
                .body("data.email", equalTo("janet.weaver@reqres.in"))
                .body("data.first_name", equalTo("Janet"))
                .body("data.last_name", equalTo("Weaver"))
                .body("data.avatar", containsString("2-image"))
                .body("support.url", notNullValue())
                .body("support.text", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify getting non-existent user returns 404")
    @Story("Get non-existent user")
    public void testGetNonExistentUser() {
        int userId = 999;
        Response response = userEndpoints.getUser(userId);
        
        response.then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify response matches JSON schema")
    @Story("Schema validation")
    public void testGetUsersSchemaValidation() {
        Response response = userEndpoints.getUsers();
        SchemaValidator.validateResponse(response, "schemas/user-list-schema.json");
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify getting users as typed object")
    @Story("Typed response")
    public void testGetUsersAsObject() {
        UserListResponse userList = userEndpoints.getUsersAsObject();
        
        Assert.assertEquals(userList.getPage(), 1);
        Assert.assertEquals(userList.getPerPage(), 6);
        Assert.assertTrue(userList.getTotal() > 0);
        Assert.assertTrue(userList.getTotalPages() > 0);
        Assert.assertEquals(userList.getData().size(), 6);
        Assert.assertNotNull(userList.getSupport());
        
        User firstUser = userList.getData().get(0);
        Assert.assertNotNull(firstUser.getId());
        Assert.assertTrue(firstUser.getEmail().contains("@"));
        Assert.assertNotNull(firstUser.getFirstName());
        Assert.assertNotNull(firstUser.getLastName());
        Assert.assertNotNull(firstUser.getAvatar());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify getting single user as typed object")
    @Story("Typed response - single user")
    public void testGetSingleUserAsObject() {
        User user = userEndpoints.getUserAsObject(2);
        
        Assert.assertEquals(user.getId(), Integer.valueOf(2));
        Assert.assertEquals(user.getEmail(), "janet.weaver@reqres.in");
        Assert.assertEquals(user.getFirstName(), "Janet");
        Assert.assertEquals(user.getLastName(), "Weaver");
    }
}
