package com.pulse.server.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @GET
    public Response getSchedule() {
        // Placeholder until TimeEdit integration is added
        return Response.ok("schedule endpoint alive").build();
    }
}
