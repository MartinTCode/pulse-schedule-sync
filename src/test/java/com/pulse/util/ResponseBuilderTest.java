package com.pulse.util;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import com.pulse.domain.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResponseBuilder utility.
 * Verifies that error responses are built correctly with proper status codes and formats.
 */
public class ResponseBuilderTest {

    @Test
    public void testErrorWithOnlyErrorCode() {
        Response response = ResponseBuilder.error(ErrorCode.CONFIG_ERROR);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getEntity());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("CONFIG_ERROR", errorResponse.getError().getCode());
        assertEquals("Missing or invalid configuration", errorResponse.getError().getMessage());
    }

    @Test
    public void testErrorWithErrorCodeAndCustomMessage() {
        Response response = ResponseBuilder.error(ErrorCode.CONFIG_ERROR, "Missing CANVAS_TOKEN");

        assertEquals(500, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("CONFIG_ERROR", errorResponse.getError().getCode());
        assertEquals("Missing CANVAS_TOKEN", errorResponse.getError().getMessage());
    }

    @Test
    public void testErrorWithErrorCodeCustomMessageAndDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("variable", "CANVAS_TOKEN");
        details.put("source", "environment");

        Response response = ResponseBuilder.error(ErrorCode.CONFIG_ERROR, "Missing configuration", details);

        assertEquals(500, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("CONFIG_ERROR", errorResponse.getError().getCode());
        assertEquals("Missing configuration", errorResponse.getError().getMessage());
        assertEquals(2, errorResponse.getError().getDetails().size());
        assertEquals("CANVAS_TOKEN", errorResponse.getError().getDetails().get("variable"));
    }

    @Test
    public void testCanvasUnauthorizedError() {
        Response response = ResponseBuilder.error(ErrorCode.CANVAS_UNAUTHORIZED);

        assertEquals(401, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("CANVAS_UNAUTHORIZED", errorResponse.getError().getCode());
    }

    @Test
    public void testCanvasUnreachableError() {
        Response response = ResponseBuilder.error(ErrorCode.CANVAS_UNREACHABLE);

        assertEquals(502, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("CANVAS_UNREACHABLE", errorResponse.getError().getCode());
    }

    @Test
    public void testTimeEditParseError() {
        Response response = ResponseBuilder.error(ErrorCode.TIMEEDIT_PARSE_ERROR);

        assertEquals(422, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("TIMEEDIT_PARSE_ERROR", errorResponse.getError().getCode());
    }

    @Test
    public void testInvalidTimeEditUrlError() {
        Response response = ResponseBuilder.error(ErrorCode.INVALID_TIMEEDIT_URL);

        assertEquals(400, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("INVALID_TIMEEDIT_URL", errorResponse.getError().getCode());
    }

    @Test
    public void testValidationErrorWithDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("field", "start");
        details.put("issue", "must be before end time");

        Response response = ResponseBuilder.error(ErrorCode.VALIDATION_ERROR, "Event validation failed", details);

        assertEquals(422, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("VALIDATION_ERROR", errorResponse.getError().getCode());
        assertEquals("Event validation failed", errorResponse.getError().getMessage());
        assertEquals("start", errorResponse.getError().getDetails().get("field"));
    }

    @Test
    public void testNullCustomMessageUsesDefault() {
        Response response = ResponseBuilder.error(ErrorCode.CANVAS_UNREACHABLE, null);

        assertEquals(502, response.getStatus());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Canvas host could not be reached", errorResponse.getError().getMessage());
    }

    @Test
    public void testAllErrorCodesProduceCorrectStatusCodes() {
        // Verify that each ErrorCode produces the correct HTTP status
        Response configError = ResponseBuilder.error(ErrorCode.CONFIG_ERROR);
        assertEquals(500, configError.getStatus());

        Response canvasAuth = ResponseBuilder.error(ErrorCode.CANVAS_UNAUTHORIZED);
        assertEquals(401, canvasAuth.getStatus());

        Response canvasUnreachable = ResponseBuilder.error(ErrorCode.CANVAS_UNREACHABLE);
        assertEquals(502, canvasUnreachable.getStatus());

        Response badRequest = ResponseBuilder.error(ErrorCode.INVALID_TIMEEDIT_URL);
        assertEquals(400, badRequest.getStatus());

        Response unprocessable = ResponseBuilder.error(ErrorCode.VALIDATION_ERROR);
        assertEquals(422, unprocessable.getStatus());
    }
}
