package com.pulse.server.resource;

import java.util.HashMap;
import java.util.Map;

import com.pulse.server.dto.CanvasPublishRequest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/canvas/publish")
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    @POST
    public Response transferSchedule(CanvasPublishRequest request) {
        // Placeholder until Canvas service is ready
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Not implemented yet");
        return Response.status(501).entity(body).build();
    }
}
