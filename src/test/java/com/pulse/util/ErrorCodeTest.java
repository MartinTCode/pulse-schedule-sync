package com.pulse.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorCode enum.
 * Verifies that all error codes are properly defined with correct HTTP status codes.
 */
public class ErrorCodeTest {

    @Test
    public void testAllErrorCodesHaveValidHttpStatus() {
        for (ErrorCode code : ErrorCode.values()) {
            assertTrue(code.getHttpStatus() >= 400 && code.getHttpStatus() < 600,
                    "HTTP status for " + code + " should be 4xx or 5xx, got " + code.getHttpStatus());
        }
    }

    @Test
    public void testAllErrorCodesHaveNonEmptyMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getDefaultMessage(), "Default message is null for " + code);
            assertFalse(code.getDefaultMessage().isEmpty(), "Default message is empty for " + code);
        }
    }

    @Test
    public void testAllErrorCodesHaveNonEmptyCode() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getCode(), "Code string is null for " + code);
            assertFalse(code.getCode().isEmpty(), "Code string is empty for " + code);
        }
    }

    @Test
    public void testConfigErrorProperties() {
        assertEquals("CONFIG_ERROR", ErrorCode.CONFIG_ERROR.getCode());
        assertEquals(500, ErrorCode.CONFIG_ERROR.getHttpStatus());
        assertEquals("Missing or invalid configuration", ErrorCode.CONFIG_ERROR.getDefaultMessage());
    }

    @Test
    public void testCanvasUnauthorizedProperties() {
        assertEquals("CANVAS_UNAUTHORIZED", ErrorCode.CANVAS_UNAUTHORIZED.getCode());
        assertEquals(401, ErrorCode.CANVAS_UNAUTHORIZED.getHttpStatus());
    }

    @Test
    public void testTimeEditParseErrorProperties() {
        assertEquals("TIMEEDIT_PARSE_ERROR", ErrorCode.TIMEEDIT_PARSE_ERROR.getCode());
        assertEquals(422, ErrorCode.TIMEEDIT_PARSE_ERROR.getHttpStatus());
    }

    @Test
    public void testValidationErrorProperties() {
        assertEquals("VALIDATION_ERROR", ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals(422, ErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @Test
    public void testCanvasUnreachableProperties() {
        assertEquals("CANVAS_UNREACHABLE", ErrorCode.CANVAS_UNREACHABLE.getCode());
        assertEquals(502, ErrorCode.CANVAS_UNREACHABLE.getHttpStatus());
    }

    @Test
    public void testInvalidTimeEditUrlProperties() {
        assertEquals("INVALID_TIMEEDIT_URL", ErrorCode.INVALID_TIMEEDIT_URL.getCode());
        assertEquals(400, ErrorCode.INVALID_TIMEEDIT_URL.getHttpStatus());
    }
}
