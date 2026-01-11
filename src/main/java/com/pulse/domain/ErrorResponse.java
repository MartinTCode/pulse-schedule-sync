package com.pulse.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standardized error response format for all API error responses.
 * Matches the API contract error format:
 *
 * {
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "Human-readable message",
 *     "details": {}
 *   }
 * }
 */
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorDetail error;

    public ErrorResponse() {
    }

    public ErrorResponse(ErrorDetail error) {
        this.error = error;
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    /**
     * Factory method to create an error response.
     *
     * @param code          The error code (use ErrorCode enum)
     * @param message       Custom message (if null, uses default from ErrorCode)
     * @param details       Additional details (can be null)
     * @return A new ErrorResponse instance
     */
    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        ErrorDetail detail = new ErrorDetail();
        detail.code = code;
        detail.message = message;
        detail.details = details != null ? details : new HashMap<>();
        return new ErrorResponse(detail);
    }

    /**
     * Nested class representing the error detail object.
     */
    public static class ErrorDetail {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("details")
        private Map<String, Object> details;

        public ErrorDetail() {
        }

        public ErrorDetail(String code, String message, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.details = details != null ? details : new HashMap<>();
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
    }
}
