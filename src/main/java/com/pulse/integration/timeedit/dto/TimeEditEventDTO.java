package com.pulse.integration.timeedit.dto;

import java.time.OffsetDateTime;

public class TimeEditEventDTO {

	private String externalId;
	private String title;
	private OffsetDateTime start;
	private OffsetDateTime end;
	private String location;
	private String description;

	public TimeEditEventDTO() {
	}

	public TimeEditEventDTO(
			String externalId,
			String title,
			OffsetDateTime start,
			OffsetDateTime end,
			String location,
			String description
	) {
		this.externalId = externalId;
		this.title = title;
		this.start = start;
		this.end = end;
		this.location = location;
		this.description = description;
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

	public OffsetDateTime getStart() {
		return start;
	}

	public void setStart(OffsetDateTime start) {
		this.start = start;
	}

	public OffsetDateTime getEnd() {
		return end;
	}

	public void setEnd(OffsetDateTime end) {
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
