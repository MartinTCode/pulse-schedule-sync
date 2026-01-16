package com.pulse.frontend.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for schedule event fields.
 * Validates required fields, time formats, and temporal consistency.
 */
public final class ScheduleEventValidator {

    // Strict HH:mm validation (e.g., 09:05, 23:59). Rejects 9:5, 24:00, etc.
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private ScheduleEventValidator() {
        
    }

    public static List<String> validate(
            String aktivitet,
            String plats,
            String beskrivning, // not required, but included for future
            LocalDate startDate,
            LocalDate endDate,
            String startTid,
            String slutTid
    ) {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (isBlank(aktivitet)) {
            errors.add("Aktivitet/Titel får inte vara tom.");
        }

        if (startDate == null) {
            errors.add("Startdatum måste vara valt.");
        }
        if (endDate == null) {
            errors.add("Slutdatum måste vara valt.");
        }

        if (isBlank(startTid)) {
            errors.add("Starttid får inte vara tom.");
        }
        if (isBlank(slutTid)) {
            errors.add("Sluttid får inte vara tom.");
        }

        // Parse times only if present
        LocalTime startTime = null;
        LocalTime endTime = null;

        if (!isBlank(startTid)) {
            startTime = tryParseTime(startTid, "Starttid", errors);
        }
        if (!isBlank(slutTid)) {
            endTime = tryParseTime(slutTid, "Sluttid", errors);
        }

        // Temporal consistency
        // Only run if we have valid dates and valid parsed times
        if (startDate != null && endDate != null && startTime != null && endTime != null) {
            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);

            // End must be after start (not equal) (covers date and time)
            if (!end.isAfter(start)) {
                errors.add("Sluttid måste vara efter starttid (slutdatum/tid måste vara senare än startdatum/tid).");
            }
        }

        return errors;
    }

    private static LocalTime tryParseTime(String timeText, String label, List<String> errors) {
        try {
            return LocalTime.parse(timeText.trim(), TIME_FMT);
        } catch (DateTimeParseException ex) {
            errors.add(label + " måste vara i formatet HH:mm (t.ex. 09:30).");
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

