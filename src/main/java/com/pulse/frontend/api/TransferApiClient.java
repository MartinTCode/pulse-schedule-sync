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
        logger.debug("Starting publishToCanvas API call");
        try {
            URI uri = URI.create(baseUrl + "/api/canvas/publish");
            logger.debug("Target URI: {}", uri);

            String json = objectMapper.writeValueAsString(requestBody);
            logger.debug("Request body serialized, length: {} bytes", json.length());
            
            byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            logger.debug("Request body as UTF-8 bytes: {} bytes", jsonBytes.length);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(jsonBytes))
                    .build();

            logger.info("Sending POST request to {} with {} bytes", uri, jsonBytes.length);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body() == null ? "" : response.body();

            logger.debug("Received response: status={}, bodyLength={}", status, body.length());

            if (status >= 200 && status < 300) {
                TransferResult result = objectMapper.readValue(body, TransferResult.class);
                logger.info("Publish finished: successes={}, failures={}",
                        result.getSuccessCount(), result.getFailureCount());
                return result;
            }

            logger.warn("Non-success status code received: {}", status);
            logger.warn("Response body: {}", body);
            
            ErrorResponse errorResponse = tryParseError(body);
            if (errorResponse != null && errorResponse.getError() != null) {
                String code = errorResponse.getError().getCode();
                String message = errorResponse.getError().getMessage();
                logger.warn("Publish failed: httpStatus={}, code={}, message={}", status, code, message);
                throw new ApiException(status, code, message);
            }

            logger.warn("Publish failed: httpStatus={}, bodyLength={}", status, body.length());
            logger.warn("Could not parse error response, raw body: {}", body);
            throw new ApiException(status, "UNKNOWN_ERROR", "Request failed (HTTP " + status + ")");
        } catch (ApiException e) {
            logger.error("API exception during publish: code={}, message={}", e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during publish API call", e);
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

