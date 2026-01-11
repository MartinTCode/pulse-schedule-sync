package com.pulse.server.resource;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pulse.util.ErrorCode;
import com.pulse.util.ResponseBuilder;
import com.pulse.integration.timeedit.TimeEditClient;

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
@Path("/api/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestErrorResource {

    private static final Logger logger = LoggerFactory.getLogger(TestErrorResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static Integer extractEventCountFromTimeEditJson(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }

        try {
            JsonNode root = mapper.readTree(rawBody);

            JsonNode reservations = root.get("reservations");
            if (reservations != null && reservations.isArray()) {
                return reservations.size();
            }

            JsonNode infoCount = root.path("info").get("reservationcount");
            if (infoCount != null && infoCount.isNumber()) {
                return infoCount.asInt();
            }

            JsonNode count = root.get("reservationcount");
            if (count != null && count.isNumber()) {
                return count.asInt();
            }
        } catch (Exception e) {
            logger.debug("Test endpoint: Failed to parse TimeEdit JSON for event count", e);
        }

        return null;
    }

    private static String truncateForLog(String text, int maxChars) {
        if (text == null) {
            return null;
        }
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...(truncated)";
    }

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
    @Path("/error/{code}")
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
    @Path("/error/{code}/{message}")
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

    /**
     * Test endpoint to fetch and log TimeEdit schedule data.
     * 
     * Usage:
     *   curl "http://localhost:8080/api/test/timeedit/fetch?url=https://cloud.timeedit.net/ltu/web/schedule1/ri176XQ0740Z5YQv050939Z6yQY855543YX6Y8gQ6086757.json"
     * 
     * This will log the fetch process in logs/pulse-*.log and logs/debug-*.log
     * 
     * @param url The TimeEdit schedule URL to fetch
     * @return JSON response with fetch success/failure
     */
    @GET
    @Path("/timeedit/fetch")
    public Response fetchTimeEdit(@QueryParam("url") String url) {
        logger.info("Test endpoint: Fetching TimeEdit from query parameter");
        if (url == null || url.trim().isEmpty()) {
            logger.warn("Test endpoint: URL parameter is empty");
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\": \"URL parameter required\"}")
                    .build();
        }
        
        logger.info("Test endpoint: Starting TimeEdit fetch for: {}", url);
        TimeEditClient.TimeEditResponse response = TimeEditClient.fetchSchedule(url);
        
        if (response.isSuccess()) {
            String body = response.getRawBody();
            logger.info("Test endpoint: TimeEdit raw response body ({} chars): {}", body.length(), truncateForLog(body, 2000));

            Integer eventCount = extractEventCountFromTimeEditJson(body);
            if (eventCount != null) {
                logger.info("Test endpoint: TimeEdit fetch successful - {} bytes retrieved, {} events received", body.length(), eventCount);
            } else {
                logger.info("Test endpoint: TimeEdit fetch successful - {} bytes retrieved", body.length());
            }

            // If the payload contains a reservations array, log a small sample for inspection.
            try {
                JsonNode root = mapper.readTree(body);
                JsonNode reservations = root.get("reservations");
                if (reservations != null && reservations.isArray()) {
                    logger.info("Test endpoint: TimeEdit reservations array size: {}", reservations.size());
                    int toLog = Math.min(10, reservations.size());
                    for (int i = 0; i < toLog; i++) {
                        logger.info("Test endpoint: TimeEdit reservation[{}]: {}", i, mapper.writeValueAsString(reservations.get(i)));
                    }
                }
            } catch (Exception e) {
                // Keep this silent-ish: body logging above already helps, and eventCount parsing logs its own debug stack.
                logger.debug("Test endpoint: Failed to parse TimeEdit JSON for reservations sample", e);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("bytesReceived", body.length());
            if (eventCount != null) {
                result.put("eventCount", eventCount);
            }

            String preview = body.substring(0, Math.min(100, body.length())) + (body.length() > 100 ? "..." : "");
            result.put("preview", preview);

            try {
                return Response.ok(mapper.writeValueAsString(result), MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {
                logger.warn("Test endpoint: Failed to build JSON response", e);
                return Response.ok().build();
            }
        } else {
            logger.warn("Test endpoint: TimeEdit fetch failed - error code: {}", response.getErrorCode());
            return Response.status(400)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\": \"" + response.getErrorCode() + "\", \"message\": \"" + 
                            response.getErrorMessage().replace("\"", "\\\"") + "\"}")
                    .build();
        }
    }
}
