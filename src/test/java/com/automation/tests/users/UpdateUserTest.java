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
@Feature("Update User")
public class UpdateUserTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "users"})
    @Description("Verify updating user with PUT")
    @Story("Update user - PUT")
    public void testUpdateUserWithPut() {
        int userId = 2;
        User updatedUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.updateUser(userId, updatedUser);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(updatedUser.getName()))
                .body("job", equalTo(updatedUser.getJob()))
                .body("updatedAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify updating user with PATCH")
    @Story("Update user - PATCH")
    public void testUpdateUserWithPatch() {
        int userId = 2;
        User updatedUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.patchUser(userId, updatedUser);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(updatedUser.getName()))
                .body("job", equalTo(updatedUser.getJob()))
                .body("updatedAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify updating user with specific data")
    @Story("Update user - specific data")
    public void testUpdateUserWithSpecificData() {
        int userId = 2;
        String name = "Jane Smith";
        String job = "Senior Developer";
        User updatedUser = TestDataFactory.userWithNameAndJob(name, job);
        
        Response response = userEndpoints.updateUser(userId, updatedUser);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(name))
                .body("job", equalTo(job))
                .body("updatedAt", notNullValue());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify updating user returns updated timestamp")
    @Story("Update user - timestamp")
    public void testUpdateUserReturnsUpdatedAt() {
        int userId = 2;
        User updatedUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.updateUser(userId, updatedUser);
        User user = response.as(User.class);
        
        Assert.assertNotNull(user.getUpdatedAt());
        Assert.assertEquals(user.getName(), updatedUser.getName());
        Assert.assertEquals(user.getJob(), updatedUser.getJob());
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify updating non-existent user")
    @Story("Update user - non-existent")
    public void testUpdateNonExistentUser() {
        int userId = 999;
        User updatedUser = TestDataFactory.randomUser();
        
        Response response = userEndpoints.updateUser(userId, updatedUser);
        
        // reqres.in returns 200 even for non-existent users
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(updatedUser.getName()))
                .body("job", equalTo(updatedUser.getJob()));
    }

    @Test(groups = {"regression", "users"})
    @Description("Verify updating user as typed object")
    @Story("Update user - typed response")
    public void testUpdateUserAsObject() {
        int userId = 2;
        User updatedUser = TestDataFactory.randomUser();
        
        User user = userEndpoints.updateUserAsObject(userId, updatedUser);
        
        Assert.assertNotNull(user.getUpdatedAt());
        Assert.assertEquals(user.getName(), updatedUser.getName());
        Assert.assertEquals(user.getJob(), updatedUser.getJob());
    }
}
