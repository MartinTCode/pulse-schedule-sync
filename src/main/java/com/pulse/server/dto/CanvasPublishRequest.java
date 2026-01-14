package com.pulse.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasPublishRequest {
    private String canvasContext;
    private PublishSchedule schedule;

    public CanvasPublishRequest() {}

    public String getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(String canvasContext) {
        this.canvasContext = canvasContext;
    }

    public PublishSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(PublishSchedule schedule) {
        this.schedule = schedule;
    }
}
