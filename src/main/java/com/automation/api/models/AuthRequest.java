package com.automation.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth request model for reqres.in API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequest {
    private String email;
    private String password;

    public static AuthRequest validCredentials() {
        return AuthRequest.builder()
                .email("eve.holt@reqres.in")
                .password("cityslicka")
                .build();
    }

    public static AuthRequest invalidCredentials() {
        return AuthRequest.builder()
                .email("invalid@reqres.in")
                .password("wrongpassword")
                .build();
    }

    public static AuthRequest missingPassword() {
        return AuthRequest.builder()
                .email("eve.holt@reqres.in")
                .build();
    }
}
