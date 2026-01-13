package com.pulse.server.resource;

import java.util.HashMap;
import java.util.Map;

import com.pulse.integration.canvas.CanvasClient;
import com.pulse.integration.canvas.CanvasPublishException;
import com.pulse.integration.canvas.CanvasUpstreamException;
import com.pulse.server.dto.CanvasPublishRequest;
import com.pulse.server.dto.PublishResult;
import com.pulse.service.CanvasPublishService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/canvas/publish")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferResource {
    private final CanvasPublishService publishService =
        new CanvasPublishService(new CanvasClient());

    // Endpoint to transfer schedule to Canvas
    @POST
    public Response transferSchedule(CanvasPublishRequest request) {
        try {
            PublishResult result = publishService.publish(request);

            int status = (result.getFailed() == 0) ? 200 : 207;

            Map<String, Object> body = new HashMap<>();
            body.put("published", result.getPublished());
            body.put("failed", result.getFailed());

            if (result.getFailed() > 0) {
                body.put("failures", result.getFailures());
            }

            return Response.status(status).entity(body).build();
        } catch (CanvasUpstreamException e) {
            int status = switch (e.getCode()) {
                case "CONFIG_ERROR" -> 500;
                case "CANVAS_UNAUTHORIZED" -> 401;
                case "CANVAS_UNREACHABLE", "CANVAS_ERROR_RESPONSE" -> 502;
                default -> 502;
            };

            return errorEnvelope(status, e.getCode(), e.getMessage());

        } catch (CanvasPublishException e) {
            return errorEnvelope(422, "VALIDATION_ERROR", e.getMessage());
        }
    }

    // Helper to create error response envelope
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
