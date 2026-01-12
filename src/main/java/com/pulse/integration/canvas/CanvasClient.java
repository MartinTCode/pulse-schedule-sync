package com.pulse.integration.canvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.net.URI;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * HTTP client for calling Canvas API.
 *
 * Responsibilities:
 * - Validate Canvas config (CANVAS_BASE_URL, CANVAS_TOKEN)
 * - Make HTTP calls to Canvas
 * - Handle network errors (CANVAS_UNREACHABLE)
 * - Handle auth errors (CANVAS_UNAUTHORIZED)
 * - Handle HTTP errors (CANVAS_ERROR_RESPONSE)
 */
public class CanvasClient {

    private static final Logger logger = LoggerFactory.getLogger(CanvasClient.class);

    private static final int TIMEOUT_SECONDS = 10;

    private final CanvasConfig config;
    private final Client client;

    public CanvasClient(CanvasConfig config) {
        this.config = config;
        this.client = ClientBuilder.newBuilder().build();
    }
}

