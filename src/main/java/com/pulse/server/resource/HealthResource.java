package com.pulse.server.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import com.pulse.integration.canvas.CanvasApiTester;
import com.pulse.integration.canvas.CanvasClient;
import com.pulse.integration.canvas.dto.CanvasUser;
import com.pulse.integration.canvas.dto.CanvasContextState;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    private final CanvasClient canvasClient = new CanvasClient();

    @GET
    public Response health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        return Response.ok(response).build();
    }

    /**
     * Tests Canvas API credentials and connectivity.
     * Calls Canvas API endpoint: GET /api/v1/users/self/profile
     * Equivalent to: curl -H "Authorization: Bearer $CANVAS_TOKEN" $CANVAS_BASE_URL/api/v1/users/self/profile
     */
    @GET
    @Path("/canvas-auth")
    public Response testCanvasApi() {
        CanvasClient.CanvasResponse<CanvasUser> result = canvasClient.testAuth();

        if (result.isSuccess()) {
            CanvasUser user = result.getData();

            String canvasContext = "user_" + user.id();
            CanvasContextState.set(canvasContext);

            Map<String, Object> canvasUser = new HashMap<>();
            canvasUser.put("id", user.id());
            canvasUser.put("login", user.login());

            Map<String, Object> body = new HashMap<>();
            body.put("canvasAuth", "OK");
            body.put("message", "Canvas API connection successful");
            body.put("canvasUser", canvasUser);

            return Response.ok(body).build();
        }

        int status = mapCanvasAuthStatus(result.getErrorCode());
        return errorEnvelope(status, result.getErrorCode(), result.getErrorMessage());
    }

    /**
     * Tests Canvas API connection and returns a standardized test result.
     * Used to verify Canvas API is accessible before attempting to publish events.
     * 
     * Response format:
     * {
     *   "success": true|false,
     *   "message": "Description of result"
     * }
     */
    @GET
    @Path("/canvas-test")
    public Response testCanvasConnection() {
        try {
            CanvasApiTester.CanvasTestResult result = CanvasApiTester.testCanvasConnection();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            int statusCode = result.isSuccess() ? 200 : 503;
            return Response.status(statusCode)
                    .entity(response)
                    .build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Canvas test failed: " + e.getMessage());
            return Response.status(503)
                    .entity(response)
                    .build();
        }
    }

    /**
     * Maps Canvas API error codes to HTTP status codes.
     * @param code Canvas error code
     * @return HTTP status code
     */
    private int mapCanvasAuthStatus(String code) {
        if (code == null) {
            return 502;
        }

        return switch (code) {
            case "CONFIG_ERROR" -> 500;
            case "CANVAS_UNAUTHORIZED" -> 401;
            case "CANVAS_UNREACHABLE", "CANVAS_ERROR_RESPONSE" -> 502;
            default -> 502;
        };
    }

    /**
     * Builds a standardized error response envelope.
     * @param httpStatus HTTP status code
     * @param code 
     * @param message 
     * @return Response object with error envelope
     */
    private Response errorEnvelope(int httpStatus, String code, String message) {
        Map<String, Object> details = new HashMap<>();

        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("details", details);

        Map<String, Object> body = new HashMap<>();
        body.put("error", error);

        return Response.status(httpStatus).entity(body).build();
    }
}
