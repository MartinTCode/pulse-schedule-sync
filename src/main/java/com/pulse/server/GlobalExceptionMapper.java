package com.pulse.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception mapper to catch and log all unhandled exceptions
 * in the REST layer, particularly deserialization errors.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Unhandled exception in REST layer", exception);
        logger.error("Exception type: {}", exception.getClass().getName());
        logger.error("Exception message: {}", exception.getMessage());
        
        if (exception.getCause() != null) {
            logger.error("Caused by: {}", exception.getCause().getClass().getName());
            logger.error("Cause message: {}", exception.getCause().getMessage());
        }

        Map<String, Object> error = new HashMap<>();
        error.put("code", "INTERNAL_SERVER_ERROR");
        error.put("message", exception.getMessage());
        error.put("type", exception.getClass().getSimpleName());

        Map<String, Object> body = new HashMap<>();
        body.put("error", error);

        return Response.status(500).entity(body).build();
    }
}
