package com.automation.utils;

import com.automation.api.models.AuthRequest;
import com.automation.api.models.Resource;
import com.automation.api.models.User;
import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;

/**
 * Test data factory using Java Faker for generating random test data
 */
public class TestDataFactory {
    private static final Faker faker = new Faker(Locale.ENGLISH);
    private static final Random random = new Random();

    private TestDataFactory() {
        // Utility class
    }

    public static User randomUser() {
        return User.builder()
                .name(faker.name().fullName())
                .job(faker.job().title())
                .build();
    }

    public static User randomUserWithEmail() {
        return User.builder()
                .name(faker.name().fullName())
                .job(faker.job().title())
                .email(faker.internet().emailAddress())
                .build();
    }

    public static User userWithNameAndJob(String name, String job) {
        return User.builder()
                .name(name)
                .job(job)
                .build();
    }

    public static AuthRequest validAuthRequest() {
        return AuthRequest.validCredentials();
    }

    public static AuthRequest invalidAuthRequest() {
        return AuthRequest.invalidCredentials();
    }

    public static AuthRequest authRequestWithEmail(String email) {
        return AuthRequest.builder()
                .email(email)
                .password(faker.internet().password(8, 16))
                .build();
    }

    public static Resource randomResource() {
        return Resource.builder()
                .name(faker.commerce().productName())
                .year(2020 + random.nextInt(5))
                .color(faker.color().name())
                .pantoneValue(String.format("#%06X", random.nextInt(0xFFFFFF)))
                .build();
    }

    public static String randomEmail() {
        return faker.internet().emailAddress();
    }

    public static String randomName() {
        return faker.name().fullName();
    }

    public static String randomJob() {
        return faker.job().title();
    }

    public static int randomId(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static int randomPage() {
        return random.nextInt(10) + 1;
    }

    public static String randomString(int length) {
        return faker.lorem().characters(length);
    }
}
