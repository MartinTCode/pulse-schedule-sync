package com.pulse.integration.canvas;

import com.pulse.server.dto.CanvasPublishRequest;
import com.pulse.server.dto.PublishSchedule;
import com.pulse.server.dto.PublishScheduleEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates the request body for POST /api/canvas/publish.
 */
public class CanvasPublishValidator {

    // Contract expects "course_<id>" or "user_<id>"
    private static final Pattern CONTEXT_PATTERN = Pattern.compile("^(course|user)_\\d+$");

    private CanvasPublishValidator() {}

    /**
     * Validates the given Canvas publish request.
     * @param request the request to validate
     */
    public static void validate(CanvasPublishRequest request) {
        // Request body is required
        if (request == null) {
            throw new CanvasPublishException("Request body is null");
        }

        // canvasContext is required and must match expected format
        String canvasContext = trimToNull(request.getCanvasContext());
        if (canvasContext == null) {
            throw new CanvasPublishException("Missing canvasContext");
        }
        if (!CONTEXT_PATTERN.matcher(canvasContext).matches()) {
            throw new CanvasPublishException(
                    "Invalid canvasContext format: " + canvasContext + " (expected course_<id> or user_<id>)"
            );
        }

        // schedule is required
        PublishSchedule schedule = request.getSchedule();
        if (schedule == null) {
            throw new CanvasPublishException("Missing schedule");
        }

        // schedule.events is required and must have at least one event
        List<PublishScheduleEvent> events = schedule.getEvents();
        if (events == null || events.isEmpty()) {
            throw new CanvasPublishException("schedule.events is null or empty");
        }

        for (PublishScheduleEvent event : events) {
            validateEvent(event);
        }
    }

    /**
     * Validates a single publish schedule event.
     * @param event the event to validate
     */
    private static void validateEvent(PublishScheduleEvent event) {
        // Event is required
        if (event == null) {
            throw new CanvasPublishException("Schedule contains null event");
        }

        String externalId = safe(event.getExternalId());

        // Title is required
        if (trimToNull(event.getTitle()) == null) {
            throw new CanvasPublishException("Event has missing title: externalId=" + externalId);
        }

        // Start/end are required
        OffsetDateTime start = event.getStart();
        OffsetDateTime end = event.getEnd();
        
        if (start == null) {
            throw new CanvasPublishException("Event has missing start: externalId=" + externalId);
        }
        
        if (end == null) {
            throw new CanvasPublishException("Event has missing end: externalId=" + externalId);
        }

        // Must have end strictly after start
        if (!end.isAfter(start)) {
            throw new CanvasPublishException(
                    "Invalid event time range: externalId=" + externalId +
                            ", start=" + start +
                            ", end=" + end
            );
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
