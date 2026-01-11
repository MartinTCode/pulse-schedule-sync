package com.pulse.server.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pulse.server.RestServer;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for TestErrorResource.
 * Validates that the test endpoint correctly returns standardized error responses.
 */
public class TestErrorResourceTest {

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
    public void testConfigErrorReturns500() {
        get("/api/test/error/CONFIG_ERROR")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("CONFIG_ERROR"))
            .body("error.message", containsString("CONFIG_ERROR"))
            .body("error.details", notNullValue());
    }

    @Test
    public void testCanvasUnauthorizedReturns401() {
        get("/api/test/error/CANVAS_UNAUTHORIZED")
            .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("CANVAS_UNAUTHORIZED"));
    }

    @Test
    public void testCanvasUnreachableReturns502() {
        get("/api/test/error/CANVAS_UNREACHABLE")
            .then()
            .statusCode(502)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("CANVAS_UNREACHABLE"));
    }

    @Test
    public void testValidationErrorReturns422() {
        get("/api/test/error/VALIDATION_ERROR")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    public void testInvalidTimeEditUrlReturns400() {
        get("/api/test/error/INVALID_TIMEEDIT_URL")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("INVALID_TIMEEDIT_URL"));
    }

    @Test
    public void testTimeEditParseErrorReturns422() {
        get("/api/test/error/TIMEEDIT_PARSE_ERROR")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("TIMEEDIT_PARSE_ERROR"));
    }

    @Test
    public void testErrorResponseHasCorrectStructure() {
        // Verify the standard error response structure
        get("/api/test/error/CONFIG_ERROR")
            .then()
            .body("error", notNullValue())
            .body("error.code", notNullValue())
            .body("error.message", notNullValue())
            .body("error.details", notNullValue());
    }

    @Test
    public void testErrorWithCustomMessage() {
        get("/api/test/error/VALIDATION_ERROR/custom_validation_message")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("VALIDATION_ERROR"))
            .body("error.message", equalTo("custom_validation_message"));
    }

    @Test
    public void testInvalidErrorCodeReturnsConfigError() {
        get("/api/test/error/INVALID_CODE_NAME")
            .then()
            .statusCode(500)
            .contentType(ContentType.JSON)
            .body("error.code", equalTo("CONFIG_ERROR"))
            .body("error.message", containsString("Unknown error code"));
    }

    @Test
    public void testAllErrorCodesReturnCorrectStatusCodes() {
        // Test a few critical ones to ensure the mapping works
        int status500 = get("/api/test/error/CONFIG_ERROR").getStatusCode();
        assert status500 == 500;

        int status401 = get("/api/test/error/CANVAS_UNAUTHORIZED").getStatusCode();
        assert status401 == 401;

        int status502 = get("/api/test/error/CANVAS_UNREACHABLE").getStatusCode();
        assert status502 == 502;

        int status400 = get("/api/test/error/INVALID_TIMEEDIT_URL").getStatusCode();
        assert status400 == 400;

        int status422 = get("/api/test/error/VALIDATION_ERROR").getStatusCode();
        assert status422 == 422;
    }

    @Test
    public void testErrorResponseJsonFormat() {
        // Verify JSON is well-formed and contains expected structure
        get("/api/test/error/CANVAS_ERROR_RESPONSE")
            .then()
            .statusCode(502)
            .contentType(ContentType.JSON)
            .body("error.code", notNullValue())
            .body("error.message", notNullValue())
            .body("error.details", instanceOf(java.util.Map.class));
    }
}
