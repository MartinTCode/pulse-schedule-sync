package com.pulse.frontend.model;

import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;

public final class AppState {

	private static volatile TimeEditScheduleDTO currentSchedule;

	private AppState() {
	}

	public static TimeEditScheduleDTO getCurrentSchedule() {
		return currentSchedule;
	}

	public static void setCurrentSchedule(TimeEditScheduleDTO schedule) {
		currentSchedule = schedule;
	}

	public static void clear() {
		currentSchedule = null;
	}
}
