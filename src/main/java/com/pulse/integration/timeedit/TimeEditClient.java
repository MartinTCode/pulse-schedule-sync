package com.pulse.integration.timeedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for fetching raw schedule data from TimeEdit.
 * 
 * Responsibilities:
 * - Validate URL format
 * - Make HTTP GET request to TimeEdit
 * - Handle network errors (TIMEEDIT_UNREACHABLE)
 * - Handle HTTP errors (TIMEEDIT_ERROR_RESPONSE)
 * - Return raw JSON response body
 */
public class TimeEditClient {

    private static final Logger logger = LoggerFactory.getLogger(TimeEditClient.class);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Fetches schedule data from TimeEdit API.
     * 
     * @param timeeditUrl The full TimeEdit schedule URL (e.g., https://cloud.timeedit.net/.../ri.json)
     * @return TimeEditResponse with raw JSON body or error details
     */
    public static TimeEditResponse fetchSchedule(String timeeditUrl) {
        // Validate URL
        if (timeeditUrl == null || timeeditUrl.trim().isEmpty()) {
            logger.warn("TimeEdit URL is null or empty");
            return TimeEditResponse.invalidUrl("URL cannot be empty");
        }

        try {
            // Validate URL format
            URI.create(timeeditUrl);  // Throws IllegalArgumentException if invalid
            logger.debug("URL format validated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid TimeEdit URL format: {} - Error: {}", timeeditUrl, e.getMessage());
            return TimeEditResponse.invalidUrl("URL format is invalid: " + e.getMessage());
        }

        try {
            // Build and execute request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(timeeditUrl))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            logger.debug("Fetching TimeEdit schedule from: {}", timeeditUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Check HTTP status
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String responseBody = response.body();
                logger.info("Successfully fetched TimeEdit schedule (HTTP {}) - {} characters received", 
                        response.statusCode(), responseBody.length());
                logger.debug("Response body preview: {}", responseBody.substring(0, Math.min(200, responseBody.length())));
                return TimeEditResponse.success(responseBody, timeeditUrl);
            } else {
                logger.warn("TimeEdit returned non-2xx status: HTTP {} for URL: {}", response.statusCode(), timeeditUrl);
                logger.debug("Error response body: {}", response.body());
                return TimeEditResponse.errorResponse(
                        "TimeEdit returned HTTP " + response.statusCode(),
                        response.statusCode()
                );
            }
        } catch (java.net.ConnectException | java.net.UnknownHostException e) {
            logger.error("Failed to connect to TimeEdit at {}: {} ({})", timeeditUrl, e.getMessage(), e.getClass().getSimpleName());
            return TimeEditResponse.unreachable("Cannot connect to TimeEdit host: " + e.getMessage());
        } catch (java.nio.file.AccessDeniedException e) {
            logger.error("Access denied when connecting to TimeEdit at {}: {}", timeeditUrl, e.getMessage());
            return TimeEditResponse.unreachable("Access denied: " + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("I/O error when fetching TimeEdit from {}: {}", timeeditUrl, e.getMessage(), e);
            return TimeEditResponse.unreachable("Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("TimeEdit fetch was interrupted after starting request to {}", timeeditUrl);
            Thread.currentThread().interrupt();
            return TimeEditResponse.unreachable("Request was interrupted");
        } catch (Exception e) {
            logger.error("Unexpected error fetching TimeEdit from {}: {}", timeeditUrl, e.getMessage(), e);
            return TimeEditResponse.unreachable("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Response object wrapping TimeEdit fetch result or error.
     */
    public static class TimeEditResponse {
        private final boolean success;
        private final String rawBody;
        private final String url;
        private final String errorCode;
        private final String errorMessage;
        private final Integer httpStatusCode;

        private TimeEditResponse(boolean success, String rawBody, String url, String errorCode, String errorMessage, Integer httpStatusCode) {
            this.success = success;
            this.rawBody = rawBody;
            this.url = url;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.httpStatusCode = httpStatusCode;
        }

        public static TimeEditResponse success(String rawBody, String url) {
            return new TimeEditResponse(true, rawBody, url, null, null, null);
        }

        public static TimeEditResponse invalidUrl(String message) {
            return new TimeEditResponse(false, null, null, "INVALID_TIMEEDIT_URL", message, null);
        }

        public static TimeEditResponse unreachable(String message) {
            return new TimeEditResponse(false, null, null, "TIMEEDIT_UNREACHABLE", message, null);
        }

        public static TimeEditResponse errorResponse(String message, int httpStatus) {
            return new TimeEditResponse(false, null, null, "TIMEEDIT_ERROR_RESPONSE", message, httpStatus);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getRawBody() {
            return rawBody;
        }

        public String getUrl() {
            return url;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Integer getHttpStatusCode() {
            return httpStatusCode;
        }
    }
}

