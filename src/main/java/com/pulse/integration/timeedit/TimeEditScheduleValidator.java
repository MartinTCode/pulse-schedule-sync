package com.pulse.integration.timeedit;

import com.pulse.integration.timeedit.dto.TimeEditEventDTO;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;

import java.time.OffsetDateTime;
import java.util.List;

public class TimeEditScheduleValidator {

	public static void validate(TimeEditScheduleDTO schedule) {
		if (schedule == null) {
			throw new TimeEditParseException("Schedule is null");
		}

		List<TimeEditEventDTO> events = schedule.getEvents();
		if (events == null) {
			throw new TimeEditParseException("Schedule events is null");
		}

		for (TimeEditEventDTO event : events) {
			if (event == null) {
				throw new TimeEditParseException("Schedule contains null event");
			}

			OffsetDateTime start = event.getStart();
			OffsetDateTime end = event.getEnd();
			if (start == null || end == null) {
				throw new TimeEditParseException(
						"Event has missing start/end: externalId=" + safe(event.getExternalId())
				);
			}

			if (!end.isAfter(start)) {
				throw new TimeEditParseException(
						"Invalid event time range: externalId=" + safe(event.getExternalId()) + ", start=" + start + ", end=" + end
				);
			}
		}
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}
}
