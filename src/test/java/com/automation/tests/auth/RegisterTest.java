package com.automation.tests.auth;

import com.automation.api.models.AuthRequest;
import com.automation.api.models.AuthResponse;
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

@Epic("Authentication")
@Feature("Register")
public class RegisterTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "auth"})
    @Description("Verify successful registration with valid credentials")
    @Story("Register - valid credentials")
    public void testSuccessfulRegister() {
        AuthRequest request = AuthRequest.builder()
                .email(TestDataFactory.randomEmail())
                .password("password123")
                .build();
        
        AuthResponse response = authEndpoints.registerAsObject(request);
        
        Assert.assertTrue(response.isSuccessful(), "Registration should be successful");
        Assert.assertNotNull(response.getToken(), "Token should not be null");
        Assert.assertFalse(response.getToken().isEmpty(), "Token should not be empty");
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify register response structure")
    @Story("Register - response structure")
    public void testRegisterResponseStructure() {
        AuthRequest request = AuthRequest.builder()
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .build();
        
        Response response = authEndpoints.register(request);
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("token", notNullValue())
                .body("token", not(emptyString()));
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify register fails with existing email")
    @Story("Register - existing email")
    public void testRegisterWithExistingEmail() {
        AuthRequest request = AuthRequest.validCredentials(); // This email already exists
        
        Response response = authEndpoints.register(request);
        
        // reqres.in returns 400 for duplicate email
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify register fails with missing email")
    @Story("Register - missing email")
    public void testRegisterWithMissingEmail() {
        AuthRequest request = AuthRequest.builder()
                .password("password123")
                .build();
        
        Response response = authEndpoints.register(request);
        
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify register fails with missing password")
    @Story("Register - missing password")
    public void testRegisterWithMissingPassword() {
        AuthRequest request = AuthRequest.builder()
                .email("test@example.com")
                .build();
        
        Response response = authEndpoints.register(request);
        
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify register response as typed object")
    @Story("Register - typed response")
    public void testRegisterAsObject() {
        AuthRequest request = AuthRequest.builder()
                .email("newuser_" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .build();
        
        AuthResponse response = authEndpoints.registerAsObject(request);
        
        Assert.assertTrue(response.isSuccessful());
        Assert.assertNotNull(response.getToken());
    }
}
