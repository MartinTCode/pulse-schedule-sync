package com.pulse.frontend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pulse.domain.ErrorResponse;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ScheduleApiClient {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleApiClient.class);

	private final String baseUrl;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public ScheduleApiClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		this.objectMapper = new ObjectMapper();
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	public TimeEditScheduleDTO fetchTimeEditSchedule(String timeeditUrl) {
		if (timeeditUrl == null || timeeditUrl.isBlank()) {
			throw new ApiException(400, "INVALID_TIMEEDIT_URL", "Missing required query parameter: timeeditUrl");
		}

		try {
			String encoded = URLEncoder.encode(timeeditUrl, StandardCharsets.UTF_8);
			URI uri = URI.create(baseUrl + "/api/timeedit/schedule?timeeditUrl=" + encoded);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.timeout(Duration.ofSeconds(45))
					.header("Accept", "application/json")
					.GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			int status = response.statusCode();
			String body = response.body() == null ? "" : response.body();

			if (status >= 200 && status < 300) {
				TimeEditScheduleDTO schedule = objectMapper.readValue(body, TimeEditScheduleDTO.class);
				logger.info(
						"Fetched schedule: events={}, rangeStart={}, rangeEnd={}, timeeditUrl={}",
						schedule.getSummary() != null ? schedule.getSummary().getEventCount() : (schedule.getEvents() != null ? schedule.getEvents().size() : 0),
						schedule.getSummary() != null ? schedule.getSummary().getRangeStart() : null,
						schedule.getSummary() != null ? schedule.getSummary().getRangeEnd() : null,
						schedule.getTimeeditUrl()
				);
				return schedule;
			}

			ErrorResponse errorResponse = tryParseError(body);
			if (errorResponse != null && errorResponse.getError() != null) {
				String code = errorResponse.getError().getCode();
				String message = errorResponse.getError().getMessage();
				logger.warn("Schedule fetch failed: httpStatus={}, code={}, message={}", status, code, message);
				throw new ApiException(status, code, message);
			}

			logger.warn("Schedule fetch failed: httpStatus={}, bodyLength={}", status, body.length());
			throw new ApiException(status, "UNKNOWN_ERROR", "Request failed (HTTP " + status + ")");
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException(0, "CLIENT_ERROR", "Failed to call server", e);
		}
	}

	private ErrorResponse tryParseError(String body) {
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(body, ErrorResponse.class);
		} catch (Exception ignored) {
			return null;
		}
	}
}
