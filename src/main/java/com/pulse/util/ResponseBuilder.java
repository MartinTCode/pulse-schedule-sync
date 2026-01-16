package com.pulse.util;

import java.util.Map;
import jakarta.ws.rs.core.Response;
import com.pulse.domain.ErrorResponse;

/**
 * Utility class for building standardized API responses.
 * Provides convenient factory methods for creating error responses that automatically:
 * - Use the correct HTTP status code from ErrorCode
 * - Apply the standardized error JSON format
 * - Handle custom messages and additional details
 *
 * Usage:
 *   return ResponseBuilder.error(ErrorCode.CANVAS_UNREACHABLE);
 *   return ResponseBuilder.error(ErrorCode.CONFIG_ERROR, "Missing CANVAS_TOKEN");
 */
public class ResponseBuilder {

    /**
     * Build an error response with full customization.
     *
     * @param errorCode     The ErrorCode enum value
     * @param customMessage Custom message (overrides default if provided)
     * @param details       Additional details map (can be null)
     * @return Response with appropriate HTTP status and error body
     */
    public static Response error(ErrorCode errorCode, String customMessage, Map<String, Object> details) {
        String message = customMessage != null ? customMessage : errorCode.getDefaultMessage();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), message, details);
        
        return Response.status(errorCode.getHttpStatus())
                .entity(errorResponse)
                .build();
    }

    /**
     * Build an error response with custom message.
     *
     * @param errorCode     The ErrorCode enum value
     * @param customMessage Custom message
     * @return Response with appropriate HTTP status and error body
     */
    public static Response error(ErrorCode errorCode, String customMessage) {
        return error(errorCode, customMessage, null);
    }

    /**
     * Build an error response using only the ErrorCode enum.
     * Uses the default message from the ErrorCode.
     *
     * @param errorCode The ErrorCode enum value
     * @return Response with appropriate HTTP status and error body
     */
    public static Response error(ErrorCode errorCode) {
        return error(errorCode, null, null);
    }
}
