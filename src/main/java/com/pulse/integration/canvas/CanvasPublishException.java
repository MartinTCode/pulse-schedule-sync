package com.pulse.integration.canvas;

/**
 * Exception thrown during Canvas publish validation or processing.
 */
public class CanvasPublishException extends RuntimeException {
    public CanvasPublishException(String message) {
        super(message);
    }
    
    public CanvasPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
