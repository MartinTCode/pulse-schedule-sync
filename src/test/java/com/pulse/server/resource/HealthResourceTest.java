package com.pulse.server.resource;

import io.restassured.RestAssured;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.pulse.server.RestServer;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public class HealthResourceTest {

    private static HttpServer server;

    @BeforeAll
    public static void setup() {
        server = RestServer.startServer();
        RestAssured.baseURI = "http://localhost:8080";
    }

    @AfterAll
    public static void cleanup() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    public void testHealthCheckEndpointReturnsOk() {
        get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    public void testHealthCheckEndpointContentType() {
        get("/health")
            .then()
            .statusCode(200)
            .contentType("application/json");
    }
}
