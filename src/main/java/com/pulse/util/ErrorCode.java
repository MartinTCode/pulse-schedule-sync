package com.pulse.util;

/**
 * Centralized enumeration of all standardized error codes used in the API.
 * Each error code is mapped to:
 * - A unique string code (for JSON responses)
 * - An HTTP status code
 * - A default human-readable message
 *
 * This ensures consistency across all endpoints and makes it easy to audit
 * against the API contract.
 */
public enum ErrorCode {
    // Configuration errors
    CONFIG_ERROR("CONFIG_ERROR", 500, "Missing or invalid configuration"),

    // Canvas errors
    CANVAS_UNREACHABLE("CANVAS_UNREACHABLE", 502, "Canvas host could not be reached"),
    CANVAS_UNAUTHORIZED("CANVAS_UNAUTHORIZED", 401, "Canvas API authentication failed (401). Check CANVAS_TOKEN."),
    CANVAS_ERROR_RESPONSE("CANVAS_ERROR_RESPONSE", 502, "Canvas returned an error response"),

    // TimeEdit errors
    INVALID_TIMEEDIT_URL("INVALID_TIMEEDIT_URL", 400, "Missing or malformed TimeEdit URL"),
    TIMEEDIT_UNREACHABLE("TIMEEDIT_UNREACHABLE", 502, "TimeEdit service could not be reached"),
    TIMEEDIT_ERROR_RESPONSE("TIMEEDIT_ERROR_RESPONSE", 502, "TimeEdit returned an error response"),
    TIMEEDIT_PARSE_ERROR("TIMEEDIT_PARSE_ERROR", 422, "Failed to parse TimeEdit response into DTO"),

    // Validation errors
    VALIDATION_ERROR("VALIDATION_ERROR", 422, "Event validation failed"),
    INVALID_PUBLISH_REQUEST("INVALID_PUBLISH_REQUEST", 400, "Missing required fields in publish request"),
    CANVAS_VALIDATION_ERROR("CANVAS_VALIDATION_ERROR", 422, "Canvas validation failed for event");

    private final String code;
    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
