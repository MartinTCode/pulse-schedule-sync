package com.pulse.server.resource;

import com.pulse.integration.timeedit.TimeEditClient;
import com.pulse.integration.timeedit.TimeEditParseException;
import com.pulse.integration.timeedit.TimeEditParser;
import com.pulse.integration.timeedit.TimeEditScheduleValidator;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;
import com.pulse.util.ErrorCode;
import com.pulse.util.ResponseBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Path("/api/timeedit/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    @GET
	// Contract: GET /api/timeedit/schedule?timeeditUrl=<full TimeEdit JSON URL>
	// - Fetch raw JSON from TimeEdit
	// - Normalize into our TimeEditScheduleDTO (events + summary)
	// - Return standardized ErrorResponse envelope on failure
    public Response getSchedule(@QueryParam("timeeditUrl") String timeeditUrl) {
		// 1) Validate client input early.
		// If the required query param is missing/blank we return a 400 with a clear message
		// (instead of attempting a fetch and failing later).
        if (timeeditUrl == null || timeeditUrl.isBlank()) {
            return ResponseBuilder.error(
                    ErrorCode.INVALID_TIMEEDIT_URL,
                    "Missing required query parameter: timeeditUrl",
                    Map.of("param", "timeeditUrl")
            );
        }

		// 2) Fetch the upstream TimeEdit JSON. The client encapsulates different failure modes
		// (invalid URL, unreachable host, non-2xx response) and we map those to our API errors.
        TimeEditClient.TimeEditResponse fetched = TimeEditClient.fetchSchedule(timeeditUrl);
        if (!fetched.isSuccess()) {
			// Include details to aid debugging without returning the raw upstream payload.
            Map<String, Object> details = new HashMap<>();
            details.put("timeeditUrl", timeeditUrl);
            if (fetched.getHttpStatusCode() != null) {
                details.put("upstreamStatus", fetched.getHttpStatusCode());
            }

            // Map TimeEditClient error categories to the API contract error codes.
            return switch (String.valueOf(fetched.getErrorCode())) {
                case "INVALID_TIMEEDIT_URL" -> ResponseBuilder.error(ErrorCode.INVALID_TIMEEDIT_URL, fetched.getErrorMessage(), details);
                case "TIMEEDIT_UNREACHABLE" -> ResponseBuilder.error(ErrorCode.TIMEEDIT_UNREACHABLE, fetched.getErrorMessage(), details);
                case "TIMEEDIT_ERROR_RESPONSE" -> ResponseBuilder.error(ErrorCode.TIMEEDIT_ERROR_RESPONSE, fetched.getErrorMessage(), details);
                default -> ResponseBuilder.error(ErrorCode.TIMEEDIT_UNREACHABLE, fetched.getErrorMessage(), details);
            };
        }

        try {
			// 3) Parse + normalize: convert TimeEdit's schema into our contract DTO.
            TimeEditScheduleDTO schedule = TimeEditParser.parseSchedule(
                    fetched.getRawBody(),
                    timeeditUrl,
                    ZoneId.systemDefault()
            );
			TimeEditScheduleValidator.validate(schedule);
            return Response.ok(schedule).build();
        } catch (TimeEditParseException e) {
			// The JSON was fetched successfully but could not be normalized (schema mismatch,
			// missing required fields, invalid date/time, etc.). Return 422 parse error.
            Map<String, Object> details = new HashMap<>();
            details.put("timeeditUrl", timeeditUrl);
            return ResponseBuilder.error(ErrorCode.TIMEEDIT_PARSE_ERROR, e.getMessage(), details);
        } catch (Exception e) {
			// Defensive catch-all: if something unexpected happens in parsing/normalization,
			// still return a 422 in the standard error envelope (no stack trace to client).
            Map<String, Object> details = new HashMap<>();
            details.put("timeeditUrl", timeeditUrl);
            return ResponseBuilder.error(ErrorCode.TIMEEDIT_PARSE_ERROR, "Failed to parse TimeEdit response", details);
        }
    }
}
