package com.pulse.integration.canvas;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration for Canvas API integration.
 * Loads environment variables from .env file.
 */
public class CanvasConfig {
    
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    
    private static final String CANVAS_TOKEN_KEY = "CANVAS_TOKEN";
    private static final String CANVAS_BASE_URL_KEY = "CANVAS_BASE_URL";
    
    /**
     * Gets the Canvas API token from environment variables.
     * Tries .env file first, then system environment.
     * @return Canvas API token, or null if not found
     */
    public static String getCanvasToken() {
        String token = dotenv.get(CANVAS_TOKEN_KEY);
        if (token == null) {
            token = System.getenv(CANVAS_TOKEN_KEY);
        }
        return token;
    }
    
    /**
     * Gets the Canvas API base URL from environment variables.
     * Tries .env file first, then system environment.
     * @return Canvas API base URL, or null if not found
     */
    public static String getCanvasBaseUrl() {
        String url = dotenv.get(CANVAS_BASE_URL_KEY);
        if (url == null) {
            url = System.getenv(CANVAS_BASE_URL_KEY);
        }
        return url;
    }
    
    /**
     * Validates that required Canvas credentials are configured.
     * @return true if both token and base URL are available
     */
    public static boolean isConfigured() {
        String token = getCanvasToken();
        String baseUrl = getCanvasBaseUrl();
        return token != null && !token.isEmpty() && baseUrl != null && !baseUrl.isEmpty();
    }
}
