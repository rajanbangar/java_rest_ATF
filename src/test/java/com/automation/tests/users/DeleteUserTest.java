package com.automation.tests.users;

import com.automation.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

@Epic("User Management")
@Feature("Delete User")
public class DeleteUserTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "users"})
    @Description("Verify deleting an existing user")
    @Story("Delete user")
    public void testDeleteUser() {
        int userId = 2;
        
        Response response = userEndpoints.deleteUser(userId);
        
        response.then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify deleting non-existent user")
    @Story("Delete non-existent user")
    public void testDeleteNonExistentUser() {
        int userId = 999;
        
        Response response = userEndpoints.deleteUser(userId);
        
        // reqres.in returns 204 even for non-existent users
        response.then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify deleting user returns no content")
    @Story("Delete user - no content")
    public void testDeleteUserNoContent() {
        int userId = 2;
        
        Response response = userEndpoints.deleteUser(userId);
        
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_NO_CONTENT);
        Assert.assertTrue(response.asString().isEmpty());
    }
}
