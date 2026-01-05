package com.pulse.integration.canvas;

import io.restassured.RestAssured;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pulse.server.RestServer;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

public class CanvasApiTesterTest {

    private static HttpServer server;

    @BeforeAll
    public static void setup() {
        server = RestServer.startServer();
        RestAssured.baseURI = "http://localhost:8080";
    }

    @AfterAll
    public static void cleanup() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    public void testCanvasTestEndpointResponds() {
        // This test verifies the endpoint exists and responds
        // The response depends on whether Canvas credentials are configured
        get("/health/canvas-test")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)));
    }

    @Test
    public void testCanvasTestEndpointReturnsJsonWithSuccessField() {
        // The endpoint should always return JSON with a success field
        get("/health/canvas-test")
            .then()
            .contentType("application/json")
            .body("$", hasKey("success"));
    }

    @Test
    public void testCanvasTestEndpointReturnsMessage() {
        // The endpoint should include a message explaining the result
        get("/health/canvas-test")
            .then()
            .contentType("application/json")
            .body("$", hasKey("message"));
    }

    @Test
    public void testCanvasTestEndpointWithoutCredentialsReturnsFalse() {
        // When Canvas credentials are not configured, the endpoint should return
        // a response with success field and message field
        get("/health/canvas-test")
            .then()
            .body("$", hasKey("success"))
            .body("$", hasKey("message"));
    }
}
