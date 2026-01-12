package com.pulse.integration.canvas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.pulse.integration.canvas.dto.CanvasUser;



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
    private final Client client;

    public CanvasClient() {
        this.client = ClientBuilder.newBuilder()
            .register(com.pulse.util.ObjectMapperContextResolver.class)
            .build();
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
     * Safely reads response body as string, truncating if too long.        
     * @param res Response object
     * @return  Body string or null if error reading
     */
    private static String safeReadBody(Response res) {
        try {
            String body = res.readEntity(String.class);
            if (body == null) return null;
            return body.length() > 500 ? body.substring(0, 500) + "..." : body;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tests Canvas API authentication by calling /api/v1/users/self/profile
     * @return CanvasResponse with CanvasUser on success, or error details
     */
    public CanvasResponse<CanvasUser> testAuth() {
        String baseUrl = getBaseUrlOrNull();
        if (baseUrl == null) {
            return CanvasResponse.configError("Missing CANVAS_BASE_URL in environment");
        }

        String token = getTokenOrNull();
        if (token == null) {
            return CanvasResponse.configError("Missing CANVAS_TOKEN in environment");
        }

        String url = baseUrl + "/api/v1/users/self/profile";
        
        try (Response res = client.target(url)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .get()) {

            int status = res.getStatus();

            if (status == 401) {
                return CanvasResponse.unauthorized(
                        "Canvas API authentication failed (401). Check CANVAS_TOKEN."
                );
            }

            if (status < 200 || status >= 300) {
                String body = safeReadBody(res);
                return CanvasResponse.errorResponse(
                        "Canvas API returned HTTP " + status + (body != null ? (": " + body) : ""),
                        status
                );
            }

            CanvasUserProfileRaw raw = res.readEntity(CanvasUserProfileRaw.class);

            String login = raw.login_id != null ? raw.login_id : raw.login;

            CanvasUser user = new CanvasUser(
                    raw.id != null ? String.valueOf(raw.id) : null,
                    login
            );

            return CanvasResponse.success(user);



        } catch (ProcessingException e) {
            return CanvasResponse.unreachable("Canvas host could not be reached: " + e.getMessage());
        }
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

