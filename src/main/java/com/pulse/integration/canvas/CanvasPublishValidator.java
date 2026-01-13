package com.pulse.integration.canvas;

import com.pulse.server.dto.CanvasPublishRequest;
import com.pulse.server.dto.PublishSchedule;
import com.pulse.server.dto.PublishScheduleEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates the request body for POST /api/canvas/publish.
 *
 * Consistent with TimeEditScheduleValidator: validate() throws a RuntimeException immediately
 * on the first invalid condition.
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

        // Start/end are required and must be ISO-8601
        OffsetDateTime start = parseIso(event.getStart(), "start", externalId);
        OffsetDateTime end = parseIso(event.getEnd(), "end", externalId);

        // Must have end strictly after start
        if (!end.isAfter(start)) {
            throw new CanvasPublishException(
                    "Invalid event time range: externalId=" + externalId +
                            ", start=" + event.getStart() +
                            ", end=" + event.getEnd()
            );
        }
    }

    /**
     * Parses the given value as an ISO-8601 OffsetDateTime.
     * @param value the value to parse
     * @param field the field name (for error messages)
     * @param externalId the event external ID (for error messages)
     * @return the parsed OffsetDateTime
     */
    private static OffsetDateTime parseIso(String value, String field, String externalId) {
        String trimmed = trimToNull(value);
        // Missing value
        if (trimmed == null) {
            throw new CanvasPublishException(
                    "Event has missing " + field + ": externalId=" + externalId
            );
        }
        try {
            return OffsetDateTime.parse(trimmed);
        // Invalid format
        } catch (DateTimeParseException e) {
            throw new CanvasPublishException(
                    "Event has invalid ISO-8601 " + field + ": externalId=" + externalId + ", value=" + value,
                    e
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
