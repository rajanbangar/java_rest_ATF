package com.automation.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User model for reqres.in API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    // For create user request
    private String name;
    private String job;

    public static User createUser(String name, String job) {
        return User.builder()
                .name(name)
                .job(job)
                .build();
    }

    public static User updateUser(String name, String job) {
        return User.builder()
                .name(name)
                .job(job)
                .build();
    }
}
