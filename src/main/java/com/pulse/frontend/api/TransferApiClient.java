package com.pulse.frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pulse.domain.ErrorResponse;
import com.pulse.domain.TransferRequest;
import com.pulse.domain.TransferResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TransferApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TransferApiClient.class);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TransferApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public TransferResult publishToCanvas(TransferRequest requestBody) {
        try {
            URI uri = URI.create(baseUrl + "/api/canvas/publish"); 

            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body() == null ? "" : response.body();

            if (status >= 200 && status < 300) {
                TransferResult result = objectMapper.readValue(body, TransferResult.class);
                logger.info("Publish finished: successes={}, failures={}",
                        result.getSuccessCount(), result.getFailureCount());
                return result;
            }

            ErrorResponse errorResponse = tryParseError(body);
            if (errorResponse != null && errorResponse.getError() != null) {
                String code = errorResponse.getError().getCode();
                String message = errorResponse.getError().getMessage();
                logger.warn("Publish failed: httpStatus={}, code={}, message={}", status, code, message);
                throw new ApiException(status, code, message);
            }

            logger.warn("Publish failed: httpStatus={}, bodyLength={}", status, body.length());
            throw new ApiException(status, "UNKNOWN_ERROR", "Request failed (HTTP " + status + ")");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "CLIENT_ERROR", "Failed to call server", e);
        }
    }

    private ErrorResponse tryParseError(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return objectMapper.readValue(body, ErrorResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}

