package com.pulse.integration.canvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;


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

    /**
     * Gets the Canvas API base URL from config, or null if not set.
     * @return Base URL string or null
     */
    private String getBaseUrlOrNull() {
        String baseUrl = CanvasConfig.getCanvasBaseUrl();
        if (baseUrl == null) return null;
        baseUrl = baseUrl.trim();
        if (baseUrl.isEmpty()) return null;
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Gets the Canvas API token from config, or null if not set.
     * @return Token string or null
     */
    private String getTokenOrNull() {
        String token = CanvasConfig.getCanvasToken();
        if (token == null) return null;
        token = token.trim();
        return token.isEmpty() ? null : token;
    }

    /**
    * Response object wrapping Canvas call result or error details
    */
    public static class CanvasResponse<T> {
        private final boolean success;
        private final String errorCode;
        private final String errorMessage;
        private final Integer httpStatusCode;
        private final T data;

        private CanvasResponse(boolean success, String errorCode, String errorMessage, Integer httpStatusCode, T data) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.httpStatusCode = httpStatusCode;
            this.data = data;
        }

        public static <T> CanvasResponse<T> success(T data) {
            return new CanvasResponse<>(true, null, null, null, data);
        }

        public static <T> CanvasResponse<T> configError(String message) {
            return new CanvasResponse<>(false, "CONFIG_ERROR", message, null, null);
        }

        public static <T> CanvasResponse<T> unauthorized(String message) {
            return new CanvasResponse<>(false, "CANVAS_UNAUTHORIZED", message, 401, null);
        }

        public static <T> CanvasResponse<T> unreachable(String message) {
            return new CanvasResponse<>(false, "CANVAS_UNREACHABLE", message, null, null);
        }

        public static <T> CanvasResponse<T> errorResponse(String message, int httpStatus) {
            return new CanvasResponse<>(false, "CANVAS_ERROR_RESPONSE", message, httpStatus, null);
        }

        public boolean isSuccess() { 
            return success; 
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

        public T getData() { 
            return data; 
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CanvasUserProfileRaw {
        public Long id;
        public String login_id;
        public String login;
    }
}

