package com.pulse.integration.canvas;

import java.util.Objects;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

public class CanvasClient {

    private final CanvasConfig config;
    private final Client client;

    public CanvasClient(CanvasConfig config) {
        this(config, ClientBuilder.newBuilder().build());
    }

    // Package-private constructor for testing
    CanvasClient(CanvasConfig config, Client client) {
        this.config = Objects.requireNonNull(config, "config");
        this.client = Objects.requireNonNull(client, "client");
    }
}
