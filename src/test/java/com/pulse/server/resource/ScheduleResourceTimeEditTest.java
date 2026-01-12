package com.pulse.server.resource;

import com.pulse.server.RestServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class ScheduleResourceTimeEditTest {

    private static org.glassfish.grizzly.http.server.HttpServer server;
    private static HttpServer mockTimeEdit;
    private static int mockTimeEditPort;

    @BeforeAll
    public static void setup() throws Exception {
        server = RestServer.startServer();
        RestAssured.baseURI = "http://localhost:8080";

        mockTimeEdit = HttpServer.create(new InetSocketAddress(0), 0);
        mockTimeEditPort = mockTimeEdit.getAddress().getPort();

        mockTimeEdit.createContext("/ok.json", new StaticJsonHandler(200, readResource("/timeedit/sample-holidays.json")));
		mockTimeEdit.createContext("/ok", new StaticJsonHandler(200, readResource("/timeedit/sample-holidays.json")));
        mockTimeEdit.createContext("/bad.json", new StaticJsonHandler(200, "this is not json"));
        mockTimeEdit.createContext("/missing-reservations.json", new StaticJsonHandler(200, "{\"columnheaders\": []}"));
        mockTimeEdit.createContext("/upstream-500.json", new StaticJsonHandler(500, "upstream error"));

        mockTimeEdit.start();
    }

    @AfterAll
    public static void cleanup() {
        if (mockTimeEdit != null) {
            mockTimeEdit.stop(0);
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void getSchedule_missingQueryParam_returns400InvalidTimeeditUrl() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("INVALID_TIMEEDIT_URL"));
    }

    @Test
    void getSchedule_malformedUrl_returns400InvalidTimeeditUrl() {
        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", "not a valid url")
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("INVALID_TIMEEDIT_URL"));
    }

    @Test
    void getSchedule_unreachableHost_returns502TimeeditUnreachable() {
        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", "http://127.0.0.1:65534/unreachable.json")
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(502)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("TIMEEDIT_UNREACHABLE"));
    }

    @Test
    void getSchedule_upstreamNon2xx_returns502TimeeditErrorResponse_andIncludesUpstreamStatus() {
        String url = "http://localhost:" + mockTimeEditPort + "/upstream-500.json";

        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", url)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(502)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("TIMEEDIT_ERROR_RESPONSE"))
                .body("error.details.upstreamStatus", equalTo(500));
    }

    @Test
    void getSchedule_badJson_returns422TimeeditParseError() {
        String url = "http://localhost:" + mockTimeEditPort + "/bad.json";

        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", url)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("TIMEEDIT_PARSE_ERROR"));
    }

    @Test
    void getSchedule_missingReservationsArray_returns422TimeeditParseError() {
        String url = "http://localhost:" + mockTimeEditPort + "/missing-reservations.json";

        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", url)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("error.code", equalTo("TIMEEDIT_PARSE_ERROR"));
    }

    @Test
    void getSchedule_success_returns200AndNormalizedSchedule() {
        String url = "http://localhost:" + mockTimeEditPort + "/ok.json";

        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", url)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("source", equalTo("TimeEdit"))
                .body("timeeditUrl", equalTo(url))
                .body("events.size()", equalTo(4))
                .body("summary.eventCount", equalTo(4))
                .body("generatedAt.length()", greaterThan(10));
    }

    @Test
    void getSchedule_inputWithoutJsonExtension_isNormalizedToJson_andStillSucceeds() {
        String inputUrl = "http://localhost:" + mockTimeEditPort + "/ok";
        String expectedNormalized = inputUrl + ".json";

        given()
                .accept(ContentType.JSON)
                .queryParam("timeeditUrl", inputUrl)
                .when()
                .get("/api/timeedit/schedule")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("source", equalTo("TimeEdit"))
                .body("timeeditUrl", equalTo(expectedNormalized))
                .body("events.size()", equalTo(4))
                .body("summary.eventCount", equalTo(4));
    }

    private static String readResource(String classpathResource) throws Exception {
        try (InputStream in = ScheduleResourceTimeEditTest.class.getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource: " + classpathResource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static final class StaticJsonHandler implements HttpHandler {
        private final int status;
        private final byte[] body;

        private StaticJsonHandler(int status, String body) {
            this.status = status;
            this.body = body.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }
    }
}
