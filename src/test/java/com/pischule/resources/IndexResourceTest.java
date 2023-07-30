package com.pischule.resources;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class IndexResourceTest {

    @Test
    void shouldContainCreateLinkInput() {
        given()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(MediaType.TEXT_HTML)
                .body(containsString("<input"), containsString("Shorten"));
    }

    @Test
    void shouldContainSignInButtonWhenAnonymous() {
        given()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(MediaType.TEXT_HTML)
                .body(containsString("Sign in"), not(containsString("My links")));
    }
}