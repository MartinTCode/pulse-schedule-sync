package com.pulse.server.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import com.pulse.integration.canvas.CanvasApiTester;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

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
        CanvasApiTester.CanvasTestResult result = CanvasApiTester.testCanvasConnection();
        
        int statusCode = result.isSuccess() ? 200 : 503;
        return Response.status(statusCode)
                .entity(result.toString())
                .build();
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
}
