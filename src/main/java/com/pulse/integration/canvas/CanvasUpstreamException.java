package com.pulse.integration.canvas;

public class CanvasUpstreamException extends RuntimeException {
    private final String code;

    public CanvasUpstreamException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
