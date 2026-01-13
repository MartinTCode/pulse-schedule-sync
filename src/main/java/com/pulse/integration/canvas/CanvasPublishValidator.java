package com.pulse.integration.canvas;

import com.pulse.server.dto.CanvasPublishRequest;
import com.pulse.server.dto.PublishSchedule;
import com.pulse.server.dto.PublishScheduleEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CanvasPublishValidator {

    private static final Pattern CONTEXT_PATTERN = Pattern.compile("^(course|user)_\\d+$");

    public ValidationResult validate(CanvasPublishRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new ValidationError("request", "Request body must not be null"));
            return new ValidationResult(errors);
        }

        // canvasContext
        if (request.getCanvasContext() == null || request.getCanvasContext().isBlank()) {
            errors.add(new ValidationError("canvasContext", "canvasContext is required"));
        } else if (!CONTEXT_PATTERN.matcher(request.getCanvasContext().trim()).matches()) {
            errors.add(new ValidationError("canvasContext", "canvasContext must match course_<id> or user_<id>"));
        }

        // schedule
        PublishSchedule schedule = request.getSchedule();
        if (schedule == null) {
            errors.add(new ValidationError("schedule", "schedule is required"));
            return new ValidationResult(errors); // can't validate events
        }

        // events
        List<PublishScheduleEvent> events = schedule.getEvents();
        if (events == null || events.isEmpty()) {
            errors.add(new ValidationError("schedule.events", "schedule.events must contain at least one event"));
            return new ValidationResult(errors);
        }

        for (int i = 0; i < events.size(); i++) {
            PublishScheduleEvent e = events.get(i);
            String base = "schedule.events[" + i + "]";

            if (e == null) {
                errors.add(new ValidationError(base, "event must not be null"));
                continue;
            }

            // title
            if (e.getTitle() == null || e.getTitle().isBlank()) {
                errors.add(new ValidationError(base + ".title", "title is required"));
            }

            // start/end
            OffsetDateTime start = null;
            OffsetDateTime end = null;

            if (e.getStart() == null || e.getStart().isBlank()) {
                errors.add(new ValidationError(base + ".start", "start is required"));
            } else {
                try {
                    start = OffsetDateTime.parse(e.getStart().trim());
                } catch (DateTimeParseException ex) {
                    errors.add(new ValidationError(base + ".start", "start must be ISO-8601 (OffsetDateTime)"));
                }
            }

            if (e.getEnd() == null || e.getEnd().isBlank()) {
                errors.add(new ValidationError(base + ".end", "end is required"));
            } else {
                try {
                    end = OffsetDateTime.parse(e.getEnd().trim());
                } catch (DateTimeParseException ex) {
                    errors.add(new ValidationError(base + ".end", "end must be ISO-8601 (OffsetDateTime)"));
                }
            }

            // start < end
            if (start != null && end != null && !start.isBefore(end)) {
                errors.add(new ValidationError(base, "start must be before end"));
            }
        }


        return new ValidationResult(errors);
    }

    // --- Result types ---

    public static class ValidationResult {
        private final List<ValidationError> errors;

        public ValidationResult(List<ValidationError> errors) {
            this.errors = errors;
        }

        public boolean isValid() {
            return errors == null || errors.isEmpty();
        }

        public List<ValidationError> getErrors() {
            return errors;
        }
    }

    public static class ValidationError {
        private final String path;     
        private final String message;  

        public ValidationError(String path, String message) {
            this.path = path;
            this.message = message;
        }

        public String getPath() { return path; }
        public String getMessage() { return message; }
    }
}

