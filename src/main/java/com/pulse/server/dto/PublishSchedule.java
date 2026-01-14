package com.pulse.server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishSchedule {
    private String source;
    private String timeeditUrl;
    private List<PublishScheduleEvent> events;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimeeditUrl() {
        return timeeditUrl;
    }

    public void setTimeeditUrl(String timeeditUrl) {
        this.timeeditUrl = timeeditUrl;
    }

    public List<PublishScheduleEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PublishScheduleEvent> events) {
        this.events = events;
    }

}
