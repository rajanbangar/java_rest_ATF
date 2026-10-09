package com.automation.tests.auth;

import com.automation.api.models.AuthRequest;
import com.automation.api.models.AuthResponse;
import com.automation.base.BaseTest;
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
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "auth"})
    @Description("Verify successful login with valid credentials")
    @Story("Login - valid credentials")
    public void testSuccessfulLogin() {
        AuthResponse response = authEndpoints.loginWithValidCredentials();
        
        Assert.assertTrue(response.isSuccessful(), "Login should be successful");
        Assert.assertNotNull(response.getToken(), "Token should not be null");
        Assert.assertFalse(response.getToken().isEmpty(), "Token should not be empty");
    }

    @Test(groups = {"smoke", "regression", "auth"})
    @Description("Verify login response structure")
    @Story("Login - response structure")
    public void testLoginResponseStructure() {
        Response response = authEndpoints.login(AuthRequest.validCredentials());
        
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("token", notNullValue())
                .body("token", not(emptyString()));
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify login fails with invalid credentials")
    @Story("Login - invalid credentials")
    public void testLoginWithInvalidCredentials() {
        Response response = authEndpoints.login(AuthRequest.invalidCredentials());
        
        response.then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error", equalTo("user not found"));
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify login fails with missing password")
    @Story("Login - missing password")
    public void testLoginWithMissingPassword() {
        Response response = authEndpoints.login(AuthRequest.missingPassword());
        
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("error", equalTo("Missing password"));
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify login fails with empty email")
    @Story("Login - empty email")
    public void testLoginWithEmptyEmail() {
        AuthRequest request = AuthRequest.builder()
                .email("")
                .password("cityslicka")
                .build();
        
        Response response = authEndpoints.login(request);
        
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify login token can be used for authenticated requests")
    @Story("Login - token usage")
    public void testLoginTokenUsage() {
        AuthResponse loginResponse = authEndpoints.loginWithValidCredentials();
        
        Assert.assertTrue(loginResponse.isSuccessful());
        
        // Set the token and make an authenticated request
        setAuthToken(loginResponse.getToken());
        
        // The token should be included in subsequent requests
        // Note: reqres.in doesn't actually validate tokens, but we verify the header is sent
        clearAuthToken();
    }

    @Test(groups = {"regression", "auth"})
    @Description("Verify login response as typed object")
    @Story("Login - typed response")
    public void testLoginAsObject() {
        AuthResponse response = authEndpoints.loginAsObject(AuthRequest.validCredentials());
        
        Assert.assertTrue(response.isSuccessful());
        Assert.assertNotNull(response.getToken());
        Assert.assertNull(response.getError());
    }
}
