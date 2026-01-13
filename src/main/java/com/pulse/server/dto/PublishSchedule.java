package com.pulse.server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishSchedule {
    private String source;
    private String timeEditUrl;
    private List<PublishScheduleEvent> events;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimeEditUrl() {
        return timeEditUrl;
    }

    public void setTimeEditUrl(String timeEditUrl) {
        this.timeEditUrl = timeEditUrl;
    }

    public List<PublishScheduleEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PublishScheduleEvent> events) {
        this.events = events;
    }

}
