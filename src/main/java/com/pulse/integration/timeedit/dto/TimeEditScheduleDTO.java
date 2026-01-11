package com.pulse.integration.timeedit.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class TimeEditScheduleDTO {

	private String source;
	private String timeeditUrl;
	private OffsetDateTime generatedAt;
	private List<TimeEditEventDTO> events;
	private TimeEditSummaryDTO summary;

	public TimeEditScheduleDTO() {
	}

	public TimeEditScheduleDTO(
			String source,
			String timeeditUrl,
			OffsetDateTime generatedAt,
			List<TimeEditEventDTO> events,
			TimeEditSummaryDTO summary
	) {
		this.source = source;
		this.timeeditUrl = timeeditUrl;
		this.generatedAt = generatedAt;
		this.events = events;
		this.summary = summary;
	}

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

	public OffsetDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(OffsetDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public List<TimeEditEventDTO> getEvents() {
		return events;
	}

	public void setEvents(List<TimeEditEventDTO> events) {
		this.events = events;
	}

	public TimeEditSummaryDTO getSummary() {
		return summary;
	}

	public void setSummary(TimeEditSummaryDTO summary) {
		this.summary = summary;
	}
}
