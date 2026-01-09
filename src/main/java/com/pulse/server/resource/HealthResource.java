package com.pulse.server.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.pulse.integration.canvas.CanvasApiTester;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        return Response.ok("{\"status\": \"ok\"}").build();
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
}
