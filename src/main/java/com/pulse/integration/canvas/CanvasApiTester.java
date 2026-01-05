package com.pulse.integration.canvas;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests Canvas API connectivity and credentials.
 * Equivalent to: curl -H "Authorization: Bearer $CANVAS_TOKEN" $CANVAS_BASE_URL/api/v1/users/self/profile
 */
public class CanvasApiTester {
    
    private static final Logger logger = LoggerFactory.getLogger(CanvasApiTester.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Tests the Canvas API credentials by fetching the current user's profile.
     * @return CanvasTestResult containing success status and details
     */
    public static CanvasTestResult testCanvasConnection() {
        logger.info("Testing Canvas API connection...");
        
        String token = CanvasConfig.getCanvasToken();
        String baseUrl = CanvasConfig.getCanvasBaseUrl();
        
        // Validate config
        if (token == null || token.isEmpty()) {
            return CanvasTestResult.failure("CANVAS_TOKEN environment variable not found in .env or system environment");
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            return CanvasTestResult.failure("CANVAS_BASE_URL environment variable not found in .env or system environment");
        }
        
        // Build the profile endpoint URL
        String profileUrl = baseUrl.replaceAll("/$", "") + "/api/v1/users/self/profile";
        logger.info("Requesting Canvas API: {}", profileUrl);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(profileUrl);
            
            // Set Authorization header with Bearer token
            httpGet.setHeader("Authorization", "Bearer " + token);
            httpGet.setHeader("Content-Type", "application/json");
            
            ClassicHttpResponse response = httpClient.executeOpen(null, httpGet, null);
            
            try {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity) : "";
                
                logger.info("Canvas API Response Status: {}", statusCode);
                
                if (statusCode == HttpStatus.SC_OK) {
                    // Parse and extract user info
                    try {
                        JsonNode jsonNode = mapper.readTree(responseBody);
                        String userId = jsonNode.has("id") ? jsonNode.get("id").asText() : "unknown";
                        String username = jsonNode.has("name") ? jsonNode.get("name").asText() : "unknown";
                        String loginId = jsonNode.has("login_id") ? jsonNode.get("login_id").asText() : "unknown";
                        
                        String message = String.format(
                            "Canvas API connection successful! User ID: %s, Name: %s, Login: %s",
                            userId, username, loginId
                        );
                        logger.info(message);
                        return CanvasTestResult.success(message);
                    } catch (Exception e) {
                        logger.warn("Could not parse user profile, but API call succeeded", e);
                        return CanvasTestResult.success("Canvas API connection successful! (response parsing failed)");
                    }
                } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    return CanvasTestResult.failure("Canvas API authentication failed (401). Check your CANVAS_TOKEN.");
                } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
                    return CanvasTestResult.failure("Canvas API access denied (403). Token may not have required permissions.");
                } else {
                    return CanvasTestResult.failure(
                        String.format("Canvas API returned status %d. Response: %s", statusCode, responseBody)
                    );
                }
            } finally {
                response.close();
            }
        } catch (java.net.ConnectException e) {
            logger.error("Failed to connect to Canvas API at: {}", baseUrl, e);
            return CanvasTestResult.failure(
                String.format("Cannot connect to Canvas API at %s. Check CANVAS_BASE_URL and network connectivity.", baseUrl)
            );
        } catch (Exception e) {
            logger.error("Unexpected error testing Canvas API", e);
            return CanvasTestResult.failure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Result of Canvas API test.
     */
    public static class CanvasTestResult {
        private final boolean success;
        private final String message;
        
        private CanvasTestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static CanvasTestResult success(String message) {
            return new CanvasTestResult(true, message);
        }
        
        public static CanvasTestResult failure(String message) {
            return new CanvasTestResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("{\"success\": %b, \"message\": \"%s\"}", success, message);
        }
    }
}
