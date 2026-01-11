package com.pulse.server.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.pulse.util.ErrorCode;
import com.pulse.util.ResponseBuilder;

/**
 * TEMPORARY TEST ENDPOINT - For validating error handling framework.
 * 
 * This endpoint demonstrates the ErrorCode enum and ResponseBuilder in action.
 * It allows testing any error code by hitting:
 *   GET /api/test/error/CONFIG_ERROR
 *   GET /api/test/error/CANVAS_UNAUTHORIZED
 *   GET /api/test/error/VALIDATION_ERROR
 *   etc.
 * 
 * DELETE THIS CLASS AFTER VALIDATING ERROR HANDLING WORKS.
 */
@Path("/api/test/error")
@Produces(MediaType.APPLICATION_JSON)
public class TestErrorResource {

    /**
     * Test endpoint for validating error responses.
     * 
     * Usage:
     *   curl http://localhost:8080/api/test/error/CONFIG_ERROR
     *   curl http://localhost:8080/api/test/error/CANVAS_UNAUTHORIZED
     * 
     * @param code The error code name (from ErrorCode enum)
     * @return HTTP response with standardized error format
     */
    @GET
    @Path("/{code}")
    public Response testError(@PathParam("code") String code) {
        try {
            ErrorCode errorCode = ErrorCode.valueOf(code);
            return ResponseBuilder.error(errorCode, "Test error: " + code);
        } catch (IllegalArgumentException e) {
            // Invalid error code
            return ResponseBuilder.error(
                ErrorCode.CONFIG_ERROR, 
                "Unknown error code: " + code
            );
        }
    }

    /**
     * Test endpoint with custom message.
     * 
     * Usage:
     *   curl http://localhost:8080/api/test/error/VALIDATION_ERROR/custom
     * 
     * @param code The error code name
     * @param message Custom message to include
     * @return HTTP response with custom message
     */
    @GET
    @Path("/{code}/{message}")
    public Response testErrorWithMessage(@PathParam("code") String code, @PathParam("message") String message) {
        try {
            ErrorCode errorCode = ErrorCode.valueOf(code);
            return ResponseBuilder.error(errorCode, message);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.error(
                ErrorCode.CONFIG_ERROR, 
                "Unknown error code: " + code
            );
        }
    }
}
