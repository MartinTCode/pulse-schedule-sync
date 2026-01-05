package com.pulse.server;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;

import com.pulse.server.resource.ScheduleResource;
import com.pulse.server.resource.TransferResource;
import com.pulse.server.resource.HealthResource;

public class RestServer {

    // Keep it simple: base host only. 
    public static final String DEFAULT_BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        return startServer(DEFAULT_BASE_URI);
    }

    public static HttpServer startServer(String baseUri) {
        final ResourceConfig rc = new ResourceConfig();

        // Register resources explicitly (predictable; no surprise scanning)
        rc.register(ScheduleResource.class);
        rc.register(TransferResource.class);
        rc.register(HealthResource.class);


        // JSON and validation
        rc.register(JacksonFeature.class);
        rc.register(ValidationFeature.class);

        // Optional: custom ObjectMapper config
        // rc.register(ObjectMapperProvider.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
    }

    public static void main(String[] args) {
        final HttpServer server = startServer();

        System.out.println("Jersey + Grizzly running at: " + DEFAULT_BASE_URI);
        System.out.println("Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted, shutting down.");
        }
    }
}
