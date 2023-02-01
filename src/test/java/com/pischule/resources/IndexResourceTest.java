package com.pischule.resources;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class IndexResourceTest {

    @Test
    void shouldContainCreateLinkInput() {
        given()
                .when().get()
                .then()
                .statusCode(200)
                .contentType(MediaType.TEXT_HTML)
                .body(hasXPath("//*[@id=\"create-link-input\"]"));
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