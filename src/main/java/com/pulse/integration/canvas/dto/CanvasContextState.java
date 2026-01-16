package com.pulse.integration.canvas.dto;

public class CanvasContextState {

    private static volatile String canvasContext;

    private CanvasContextState() {}

    public static void set(String context) {
        canvasContext = context;

    }

    public static String get() {
        return canvasContext;
    }
    
}
