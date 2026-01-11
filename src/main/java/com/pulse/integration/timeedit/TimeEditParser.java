package com.pulse.integration.timeedit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.integration.timeedit.dto.TimeEditEventDTO;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;
import com.pulse.integration.timeedit.dto.TimeEditSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TimeEditParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static TimeEditScheduleDTO parseSchedule(String rawJson, String timeeditUrl) {
		return parseSchedule(rawJson, timeeditUrl, ZoneId.systemDefault());
	}

	public static TimeEditScheduleDTO parseSchedule(String rawJson, String timeeditUrl, ZoneId zoneId) {
		
        // basic requirments
        if (rawJson == null || rawJson.isBlank()) {
			throw new TimeEditParseException("TimeEdit response body is empty");
		}
		if (zoneId == null) {
			throw new TimeEditParseException("ZoneId cannot be null");
		}

		try {

            // parse JSON while keeping it from breaking if unknown fields show up.
			JsonNode root = objectMapper.readTree(rawJson);

            // read column headers to find indices of optional fields
			List<String> headers = readStringArray(root.get("columnheaders"));
			
            int locationIndex = findHeaderIndex(headers, "location");
			Integer commentIndex = findHeaderIndexOrNull(headers, "comment");
			Integer textIndex = findHeaderIndexOrNull(headers, "text");

            // make sure reservations array exists
			JsonNode reservationsNode = root.get("reservations");
			if (reservationsNode == null || !reservationsNode.isArray()) {
				throw new TimeEditParseException("Missing or invalid 'reservations' array");
			}

            // parse each reservation (calendar event) into TimeEditEventDTO
			List<TimeEditEventDTO> events = new ArrayList<>();
			for (JsonNode reservation : reservationsNode) {
                // required fields

				String id = readRequiredText(reservation, "id");
				// read date and time fields
                String startDate = readRequiredText(reservation, "startdate");
				String startTime = readRequiredText(reservation, "starttime");
				String endDate = readRequiredText(reservation, "enddate");
				String endTime = readRequiredText(reservation, "endtime");



                // convert to OffsetDateTime
				OffsetDateTime start = toOffsetDateTime(startDate, startTime, zoneId);
				OffsetDateTime end = toOffsetDateTime(endDate, endTime, zoneId);
                // get all column values (e.g. "Långfredagen",, etc)
				List<String> columns = readStringArray(reservation.get("columns"));

                // get the title from the first column, default to (untitled) if blank
				String title = safeTrim(getColumnValue(columns, 0));
				if (title.isEmpty()) {
					title = "(untitled)";
				}

				String location = "";
                // find location if index available (-1 means not found)
				if (locationIndex >= 0) {
					location = safeTrim(getColumnValue(columns, locationIndex));
				}
                // Sets description to the first non-blank value from 
                // the reservation’s “Comment” column (preferred) or 
                // “Text” column, trimmed; otherwise ""
				String description = "";
				description = firstNonBlank(
						normalizeFreeText(commentIndex != null ? getColumnValue(columns, commentIndex) : ""),
						normalizeFreeText(textIndex != null ? getColumnValue(columns, textIndex) : "")
				);

                // create event DTO
				TimeEditEventDTO event = new TimeEditEventDTO(
						"TE-" + id,
						title,
						start,
						end,
						location,
						description
				);
				events.add(event);
			}

            // create summary DTO
			OffsetDateTime rangeStart = events.stream()
					.map(TimeEditEventDTO::getStart)
					.filter(Objects::nonNull)
					.min(Comparator.naturalOrder())
					.orElse(null);

			OffsetDateTime rangeEnd = events.stream()
					.map(TimeEditEventDTO::getEnd)
					.filter(Objects::nonNull)
					.max(Comparator.naturalOrder())
					.orElse(null);

			TimeEditSummaryDTO summary = new TimeEditSummaryDTO(events.size(), rangeStart, rangeEnd);
			// create and return schedule DTO
            return new TimeEditScheduleDTO(
					"TimeEdit",
					timeeditUrl,
					OffsetDateTime.now(zoneId),
					events,
					summary
			);
		} catch (TimeEditParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TimeEditParseException("Failed to parse TimeEdit JSON", e);
		}
	}

    // Convert date and time strings to OffsetDateTime using the given ZoneId
	private static OffsetDateTime toOffsetDateTime(String date, String time, ZoneId zoneId) {
		try {
			LocalDate d = LocalDate.parse(date);
			LocalTime t = LocalTime.parse(time);
			LocalDateTime local = LocalDateTime.of(d, t);
			ZonedDateTime zoned = ZonedDateTime.of(local, zoneId);
			return zoned.toOffsetDateTime();
		} catch (Exception e) {
			throw new TimeEditParseException("Invalid date/time: " + date + " " + time, e);
		}
	}

    // Read required text field or throw exception if missing or blank
	private static String readRequiredText(JsonNode node, String fieldName) {
		JsonNode value = node.get(fieldName);
		if (value == null || value.isNull()) {
			throw new TimeEditParseException("Missing required field: " + fieldName);
		}
		String text = value.asText("");
		if (text.isBlank()) {
			throw new TimeEditParseException("Blank required field: " + fieldName);
		}
		return text;
	}

    // Read string array or return empty list if missing or null
    // throws exception if not an array
    // 
	private static List<String> readStringArray(JsonNode node) {
		List<String> result = new ArrayList<>();
		if (node == null || node.isNull()) {
			return result;
		}
		if (!node.isArray()) {
			throw new TimeEditParseException("Expected JSON array");
		}
        // read each item as text then add to result list
		for (JsonNode item : node) {
			result.add(item.isNull() ? "" : item.asText(""));
		}
		return result;
	}

    // Find header index by case-insensitive substring match or -1 if not found
	private static int findHeaderIndex(List<String> headers, String needleLower) {
		Integer idx = findHeaderIndexOrNull(headers, needleLower);
		return idx != null ? idx : -1;
	}

    // Find header index or return null if not found
	private static Integer findHeaderIndexOrNull(List<String> headers, String needleLower) {
		if (headers == null || headers.isEmpty()) {
			return null;
		}
		for (int i = 0; i < headers.size(); i++) {
			String h = headers.get(i);
			if (h != null && h.toLowerCase().contains(needleLower)) {
				return i;
			}
		}
		return null;
	}

	private static String getColumnValue(List<String> columns, int index) {
		if (columns == null || index < 0 || index >= columns.size()) {
			return "";
		}
		return columns.get(index);
	}

	private static String safeTrim(String s) {
		return s == null ? "" : s.trim();
	}

	private static String normalizeFreeText(String s) {
		if (s == null) {
			return "";
		}
		// TimeEdit sometimes represents an "empty" text field as ", " (comma + whitespace).
		// Treat strings made only of commas/whitespace as empty.
		boolean onlyCommasOrWhitespace = true;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != ',' && !Character.isWhitespace(c)) {
				onlyCommasOrWhitespace = false;
				break;
			}
		}
		if (onlyCommasOrWhitespace) {
			return "";
		}
		return s.trim();
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return "";
		}
		for (String v : values) {
			if (v != null && !v.isBlank()) {
				return v;
			}
		}
		return "";
	}

}
