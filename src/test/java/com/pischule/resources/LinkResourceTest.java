package com.pischule.resources;

import com.pischule.model.Link;
import com.pischule.services.LinkService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class LinkResourceTest {
    @InjectMock
    LinkService linkService;

    @Test
    public void testRedirect() {
        Mockito.when(linkService.incrementVisitsAndGetUri(Mockito.eq("aaaaaa")))
                .thenReturn(URI.create("https://example.com"));

        given()
                .redirects().follow(false)
                .when().get("/aaaaaa")
                .then()
                .statusCode(303)
                .header("Location", "https://example.com")
                .contentType(ContentType.HTML)
                .body(containsString("Redirecting to"),
                        containsString("<a href=\"https://example.com\">https://example.com</a>"));
    }

    @Test
    public void testRedirectNotFound() {
        Mockito.when(linkService.incrementVisitsAndGetUri(Mockito.anyString()))
                .thenThrow(new NotFoundException("Link not found"));

        given()
                .when().get("/aaaaaa")
                .then()
                .statusCode(404)
                .contentType(ContentType.HTML)
                .body(containsString("404"), containsString("Link not found"));
    }

    @Test
    public void testView() {
        Mockito.when(linkService.getById(Mockito.eq("aaaaaa")))
                .thenReturn(link(true));

        given()
                .when().get("/aaaaaa/view")
                .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("http://localhost:8080/aaaaaa"),
                        containsStringIgnoringCase("copy"),
                        containsString("Destination: <a href=\"https://example.com\">https://example.com</a>"),
                        containsString("Total visits: 5"),
                        containsString("Created at: 2023-01-01T00:00:00Z"));
    }

    @Test
    public void testViewWhenOwner() {
        Mockito.when(linkService.getById("aaaaaa"))
                .thenReturn(link(true));

        given()
                .when().get("/aaaaaa/view")
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("edit"));
    }

    @Test
    public void testViewWhenNotOwner() {
        Mockito.when(linkService.getById("aaaaaa"))
                .thenReturn(link(false));

        given()
                .when().get("/aaaaaa/view")
                .then()
                .statusCode(200)
                .body(not(containsStringIgnoringCase("edit")));
    }

    @Test
    @TestSecurity(user = "alice")
    public void testGetEdit() {
        Mockito.when(linkService.getById("aaaaaa"))
                .thenReturn(link(true));

        given()
                .when().get("/aaaaaa/edit")
                .then()
                .statusCode(200)
                .body(
                        containsString("https://example.com"),
                        containsStringIgnoringCase("cancel"),
                        containsStringIgnoringCase("edit"),
                        containsStringIgnoringCase("delete")
                );
    }

    @Test
    @TestSecurity
    public void testGetEditUnauthorized() {
        given()
                .redirects().follow(false)
                .when().get("/aaaaaa/edit")
                .then()
                .statusCode(302)
                .log().all();
    }

    private Link link(boolean isOwner) {
        return new Link("aaaaaa",
                "https://example.com",
                "http://localhost:8080/aaaaaa",
                5,
                Instant.parse("2023-01-01T00:00:00Z"),
                isOwner);
    }
}
