package com.automation.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;

/**
 * Reusable response specifications for validation
 */
public class ResponseSpecs {

    public static ResponseSpecification successSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType("application/json")
                .expectHeader("Content-Type", Matchers.containsString("application/json"))
                .build();
    }

    public static ResponseSpecification createdSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType("application/json")
                .build();
    }

    public static ResponseSpecification noContentSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(204)
                .build();
    }

    public static ResponseSpecification badRequestSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .expectContentType("application/json")
                .build();
    }

    public static ResponseSpecification unauthorizedSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(401)
                .expectContentType("application/json")
                .build();
    }

    public static ResponseSpecification notFoundSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .expectContentType("application/json")
                .build();
    }

    public static ResponseSpecification serverErrorSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(Matchers.anyOf(Matchers.equalTo(500), Matchers.equalTo(502), Matchers.equalTo(503)))
                .build();
    }
}
