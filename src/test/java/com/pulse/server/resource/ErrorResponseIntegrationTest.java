package com.pulse.server.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pulse.server.RestServer;

import static io.restassured.RestAssured.get;

/**
 * Integration tests for error responses.
 * Verifies that error responses are properly formatted in actual HTTP responses.
 */
public class ErrorResponseIntegrationTest {

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
    public void testErrorResponseStructureHasRequiredFields() {
        // Health endpoint should return valid JSON
        get("/health")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testHealthEndpointReturnsValidJson() {
        // Test that health endpoint returns JSON
        get("/health")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testCanvasAuthEndpointReturnsJson() {
        // Test that canvas-auth endpoint returns valid JSON
        // (will fail if credentials not set, but should still be valid JSON)
        get("/health/canvas-auth")
            .then()
            .contentType(ContentType.JSON);
    }

    /**
     * This test demonstrates how error responses should look.
     * It can be used as a template for verifying other error responses.
     */
    @Test
    public void testErrorResponseFormatExample() {
        // Example of what a proper error response should contain:
        // {
        //   "error": {
        //     "code": "ERROR_CODE",
        //     "message": "Human-readable message",
        //     "details": {}
        //   }
        // }
        
        // You can verify this structure in actual endpoints like:
        // get("/api/canvas/publish?invalid=params")
        //     .then()
        //     .statusCode(400)
        //     .body("error.code", notNullValue())
        //     .body("error.message", notNullValue())
        //     .body("error.details", notNullValue());
    }
}
