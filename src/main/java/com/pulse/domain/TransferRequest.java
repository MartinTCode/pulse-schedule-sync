package com.pulse.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Request sent from GUI -> Server when publishing events to Canvas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {

    @JsonProperty("canvasContext")
    private String canvasContext;

    @JsonProperty("schedule")
    private Schedule schedule = new Schedule();

    public TransferRequest() {
    }

    public TransferRequest(String canvasContext, Schedule schedule) {
        this.canvasContext = canvasContext;
        this.schedule = schedule;
    }

    public String getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(String canvasContext) {
        this.canvasContext = canvasContext;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    //Nested DTOs to avoid creating more files

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schedule {

        @JsonProperty("events")
        private List<ScheduleEvent> events = new ArrayList<>();

        public Schedule() {
        }

        public Schedule(List<ScheduleEvent> events) {
            this.events = events;
        }

        public List<ScheduleEvent> getEvents() {
            return events;
        }

        public void setEvents(List<ScheduleEvent> events) {
            this.events = events;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScheduleEvent {

        /**
         * Used by the backend as identifier in failures (Failure(event.getExternalId(), msg)).
         */
        @JsonProperty("externalId")
        private String externalId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("start")
        private java.time.OffsetDateTime start;

        @JsonProperty("end")
        private java.time.OffsetDateTime end;

        @JsonProperty("location")
        private String location;

        @JsonProperty("description")
        private String description;

        public ScheduleEvent() {
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public java.time.OffsetDateTime getStart() {
            return start;
        }

        public void setStart(java.time.OffsetDateTime start) {
            this.start = start;
        }

        public java.time.OffsetDateTime getEnd() {
            return end;
        }

        public void setEnd(java.time.OffsetDateTime end) {
            this.end = end;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
