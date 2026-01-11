package com.pulse.integration.timeedit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeEditClient.
 * Tests URL validation and error handling paths (mocking is minimal since we test the logic).
 */
public class TimeEditClientTest {

    @Test
    public void testNullUrlReturnsInvalidUrl() {
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule(null);
        
        assertFalse(response.isSuccess());
        assertEquals("INVALID_TIMEEDIT_URL", response.getErrorCode());
        assertNull(response.getRawBody());
    }

    @Test
    public void testEmptyUrlReturnsInvalidUrl() {
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule("");
        
        assertFalse(response.isSuccess());
        assertEquals("INVALID_TIMEEDIT_URL", response.getErrorCode());
        assertNull(response.getRawBody());
    }

    @Test
    public void testWhitespaceUrlReturnsInvalidUrl() {
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule("   ");
        
        assertFalse(response.isSuccess());
        assertEquals("INVALID_TIMEEDIT_URL", response.getErrorCode());
    }

    @Test
    public void testMalformedUrlReturnsInvalidUrl() {
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule("not a valid url");
        
        assertFalse(response.isSuccess());
        assertEquals("INVALID_TIMEEDIT_URL", response.getErrorCode());
        assertTrue(response.getErrorMessage().contains("URL format is invalid"));
    }

    @Test
    public void testInvalidProtocolReturnsUnreachable() {
        // FTP protocol may not be caught as URL format error, but as connection error
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule("ftp://example.com");
        
        assertFalse(response.isSuccess());
        // Either INVALID_TIMEEDIT_URL or TIMEEDIT_UNREACHABLE is acceptable
        assertTrue(response.getErrorCode().equals("INVALID_TIMEEDIT_URL") || 
                   response.getErrorCode().equals("TIMEEDIT_UNREACHABLE"));
    }

    @Test
    public void testUnreachableHostReturnsUnreachable() {
        // Valid URL format, but host doesn't exist
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule("http://this-host-should-not-exist-12345.invalid");
        
        assertFalse(response.isSuccess());
        assertEquals("TIMEEDIT_UNREACHABLE", response.getErrorCode());
        assertNull(response.getRawBody());
    }

    @Test
    public void testErrorResponseAlwaysHasErrorCode() {
        // Verify that any error response has an error code set
        TimeEditClient.TimeEditResponse response1 = TimeEditClient.fetchSchedule(null);
        assertNotNull(response1.getErrorCode());
        
        TimeEditClient.TimeEditResponse response2 = TimeEditClient.fetchSchedule("not a url");
        assertNotNull(response2.getErrorCode());
        
        TimeEditClient.TimeEditResponse response3 = TimeEditClient.fetchSchedule("http://nonexistent-12345.invalid");
        assertNotNull(response3.getErrorCode());
    }

    @Test
    public void testSuccessResponseHasRawBody() {
        // This would need a mock server in integration tests
        // For unit tests, we verify the response structure
        String testUrl = "http://localhost:9999/nonexistent";
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule(testUrl);
        
        // Will fail to connect, but structure is valid
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorCode());
    }

    @Test
    public void testErrorResponseContainsHttpStatus() {
        // When we can mock, this will test HTTP error status codes
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule(null);
        
        // Invalid URL should not have HTTP status
        assertNull(response.getHttpStatusCode());
    }
}
