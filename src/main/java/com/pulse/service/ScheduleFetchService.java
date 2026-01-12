package com.pulse.service;

import com.pulse.integration.timeedit.TimeEditClient;
import com.pulse.integration.timeedit.TimeEditFetchException;
import com.pulse.integration.timeedit.TimeEditParser;
import com.pulse.integration.timeedit.TimeEditScheduleValidator;
import com.pulse.integration.timeedit.TimeEditUrlNormalizer;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;
import com.pulse.util.ErrorCode;

import java.time.ZoneId;

public class ScheduleFetchService {

	public static TimeEditScheduleDTO fetchAndParseTimeEditSchedule(String timeeditUrl, ZoneId zoneId) {
		String normalizedUrl;
		try {
			normalizedUrl = TimeEditUrlNormalizer.ensureJsonUrl(timeeditUrl);
		} catch (IllegalArgumentException e) {
			throw new TimeEditFetchException(ErrorCode.INVALID_TIMEEDIT_URL, "URL format is invalid: " + e.getMessage(), null);
		}

		TimeEditClient.TimeEditResponse fetched = TimeEditClient.fetchSchedule(normalizedUrl);
		if (!fetched.isSuccess()) {
			ErrorCode errorCode = switch (String.valueOf(fetched.getErrorCode())) {
				case "INVALID_TIMEEDIT_URL" -> ErrorCode.INVALID_TIMEEDIT_URL;
				case "TIMEEDIT_UNREACHABLE" -> ErrorCode.TIMEEDIT_UNREACHABLE;
				case "TIMEEDIT_ERROR_RESPONSE" -> ErrorCode.TIMEEDIT_ERROR_RESPONSE;
				default -> ErrorCode.TIMEEDIT_UNREACHABLE;
			};

			throw new TimeEditFetchException(errorCode, fetched.getErrorMessage(), fetched.getHttpStatusCode());
		}

		TimeEditScheduleDTO schedule = TimeEditParser.parseSchedule(
				fetched.getRawBody(),
				normalizedUrl,
				zoneId
		);
		TimeEditScheduleValidator.validate(schedule);
		return schedule;
	}
}
