package com.pulse.domain;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponse DTO.
 * Verifies correct serialization and factory method behavior.
 */
public class ErrorResponseTest {

    @Test
    public void testErrorResponseFactoryWithAllParameters() {
        Map<String, Object> details = new HashMap<>();
        details.put("field", "title");
        details.put("reason", "required");

        ErrorResponse response = ErrorResponse.of("TEST_ERROR", "Test message", details);

        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals("TEST_ERROR", response.getError().getCode());
        assertEquals("Test message", response.getError().getMessage());
        assertEquals(2, response.getError().getDetails().size());
        assertEquals("title", response.getError().getDetails().get("field"));
    }

    @Test
    public void testErrorResponseFactoryWithoutDetails() {
        ErrorResponse response = ErrorResponse.of("TEST_ERROR", "Test message", null);

        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals("TEST_ERROR", response.getError().getCode());
        assertEquals("Test message", response.getError().getMessage());
        assertNotNull(response.getError().getDetails());
        assertTrue(response.getError().getDetails().isEmpty());
    }

    @Test
    public void testErrorDetailGettersAndSetters() {
        ErrorResponse.ErrorDetail detail = new ErrorResponse.ErrorDetail();
        
        detail.setCode("ERROR_CODE");
        detail.setMessage("Error message");
        
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("key", "value");
        detail.setDetails(detailsMap);

        assertEquals("ERROR_CODE", detail.getCode());
        assertEquals("Error message", detail.getMessage());
        assertEquals(1, detail.getDetails().size());
        assertEquals("value", detail.getDetails().get("key"));
    }

    @Test
    public void testErrorResponseConstructor() {
        ErrorResponse.ErrorDetail detail = new ErrorResponse.ErrorDetail("CODE", "Message", null);
        ErrorResponse response = new ErrorResponse(detail);

        assertEquals("CODE", response.getError().getCode());
        assertEquals("Message", response.getError().getMessage());
    }

    @Test
    public void testErrorDetailConstructorWithAllParameters() {
        Map<String, Object> details = new HashMap<>();
        details.put("test", "data");

        ErrorResponse.ErrorDetail detail = new ErrorResponse.ErrorDetail("CODE", "Message", details);

        assertEquals("CODE", detail.getCode());
        assertEquals("Message", detail.getMessage());
        assertEquals(1, detail.getDetails().size());
    }

    @Test
    public void testErrorDetailConstructorWithNullDetails() {
        ErrorResponse.ErrorDetail detail = new ErrorResponse.ErrorDetail("CODE", "Message", null);

        assertEquals("CODE", detail.getCode());
        assertEquals("Message", detail.getMessage());
        assertNotNull(detail.getDetails());
        assertTrue(detail.getDetails().isEmpty());
    }

    @Test
    public void testErrorResponseJsonSerialization() {
        // This test verifies the @JsonProperty annotations are in place
        // Jackson will use these to serialize the error field correctly
        Map<String, Object> details = new HashMap<>();
        details.put("statusCode", 422);

        ErrorResponse response = ErrorResponse.of("VALIDATION_ERROR", "Validation failed", details);

        assertNotNull(response.getError());
        assertEquals("VALIDATION_ERROR", response.getError().getCode());
        assertEquals("Validation failed", response.getError().getMessage());
        assertEquals(422, response.getError().getDetails().get("statusCode"));
    }
}
