package com.pulse.server.resource;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/transfer")
@Produces(MediaType.APPLICATION_JSON)
public class TransferResource {

    @POST
    public Response transferSchedule() {
        // Placeholder until Canvas integration is added
        return Response.ok("transfer endpoint alive").build();
    }
}
