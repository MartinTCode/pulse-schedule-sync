package com.pulse.integration.canvas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.pulse.integration.canvas.dto.CanvasCalendarEventRequest;
import com.pulse.integration.canvas.dto.CanvasCalendarEventResponse;
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
    private static final Logger logger = LoggerFactory.getLogger(CanvasClient.class);
    
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

    public CanvasUser getAuthenticatedUser() {
        CanvasResponse<CanvasUser> result = testAuth();

        if (!result.isSuccess()) {
            throw new CanvasUpstreamException(result.getErrorCode(), "Failed to resolve Canvas user");

        }
        return result.getData();

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
     * Creates a calendar event in Canvas.
     * @param req the calendar event request
     * @return CanvasResponse with CanvasCalendarEventResponse on success, or error details
     */
    public CanvasResponse<CanvasCalendarEventResponse> createCalendarEvent(CanvasCalendarEventRequest req) {
        logger.debug("createCalendarEvent called");
        
        // Validate request 
        if (req == null) {
            logger.error("Request is null");
            return CanvasResponse.errorResponse("Request is null", 400);
        }

        String baseUrl = getBaseUrlOrNull();
        if (baseUrl == null) {
            logger.error("Missing CANVAS_BASE_URL in environment");
            return CanvasResponse.configError("Missing CANVAS_BASE_URL in environment");
        }

        String token = getTokenOrNull();
        if (token == null) {
            logger.error("Missing CANVAS_TOKEN in environment");
            return CanvasResponse.configError("Missing CANVAS_TOKEN in environment");
        }

        // Build URL
        String url = baseUrl + "/api/v1/calendar_events";
        logger.debug("Creating calendar event at URL: {}", url);
        logger.debug("Event details: context={}, title={}, start={}, end={}", 
            req.getContextCode(), req.getTitle(), req.getStartAt(), req.getEndAt());

        // Build form parameters
        Form form = new Form()
                .param("calendar_event[context_code]", req.getContextCode())
                .param("calendar_event[title]", req.getTitle())
                .param("calendar_event[start_at]", req.getStartAt())
                .param("calendar_event[end_at]", req.getEndAt());

        if (req.getLocationName() != null && !req.getLocationName().isBlank()) {
            form.param("calendar_event[location_name]", req.getLocationName());
            logger.debug("Location: {}", req.getLocationName());
        }

        if (req.getDescription() != null && !req.getDescription().isBlank()) {
            form.param("calendar_event[description]", req.getDescription());
            logger.debug("Description length: {} chars", req.getDescription().length());
        }

        try (Response res = client.target(url)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE))) {

            int status = res.getStatus();
            logger.debug("Canvas API response status: {}", status);

            if (status == 401) {
                logger.error("Canvas authentication failed (401)");
                return CanvasResponse.unauthorized("Canvas API authentication failed (401). Check CANVAS_TOKEN.");
            }

            if (status < 200 || status >= 300) {
                String body = safeReadBody(res);
                logger.warn("Canvas API error response: status={}, body={}", status, body);
                return CanvasResponse.errorResponse(
                        "Canvas API returned HTTP " + status + (body != null ? (": " + body) : ""),
                        status
                );
            }

            CanvasCalendarEventResponse created = res.readEntity(CanvasCalendarEventResponse.class);
            logger.info("Calendar event created successfully: id={}, title={}", 
                created != null ? created.getId() : "unknown");
            return CanvasResponse.success(created);

        } catch (ProcessingException e) {
            logger.error("Canvas host unreachable: {}", e.getMessage(), e);
            return CanvasResponse.unreachable("Canvas host could not be reached: " + e.getMessage());
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

