
# Implementation Notes: TimeEdit Schedule Endpoint

This document describes the practical implementation choices for the contract endpoint:

- `GET /api/timeedit/schedule?timeeditUrl=...`

It complements (but does not replace) the API contract in [docs/api-contract.md](../api-contract.md).

## Goal (scope)

- Fetch a client-provided TimeEdit JSON URL.
- Normalize it into the schedule response DTO described in the API contract.
- Do **not** return TimeEdit’s raw JSON fields in the response (e.g. `reservations`, `columnheaders`, or any `rawBody`/preview). Only return the normalized contract fields (`events`, `summary`, etc.).

## Endpoint

- **Path:** `GET /api/timeedit/schedule`
- **Required query param:** `timeeditUrl` (full TimeEdit schedule URL)
- **Success (200):** returns a normalized schedule with `events[]` and `summary`.

## Implementation outline

1. `ScheduleResource` validates `timeeditUrl` is present.
2. `ScheduleFetchService` calls `TimeEditClient.fetchSchedule(timeeditUrl)`.
3. On HTTP success, `TimeEditParser` parses the raw TimeEdit payload:
	 - reads `reservations[]`
	 - maps each reservation into a contract `event`
4. Compute `summary`:
	 - `eventCount = events.size()`
	 - `rangeStart = min(event.start)`
	 - `rangeEnd = max(event.end)`
5. Return JSON (Jersey/Jackson serialization).

## Mapping assumptions (TimeEdit → contract event)

TimeEdit payload of interest:

- `reservations[i].id`
- `reservations[i].startdate`, `starttime`, `enddate`, `endtime`
- `reservations[i].columns[]`
- `columnheaders[]` (used only to help interpret `columns[]`)

Contract event fields:

- `externalId`
	- Use the upstream reservation id, prefixed for clarity: `TE-<id>`.
- `start` / `end`
	- Combine date + time into the server’s ISO-8601 format with local offset.
	- If end is missing, treat it as a parse error (422).
- `title`
	- Prefer `columns[0]` (typically "Activity").
	- If empty, fall back to `"(untitled)"`.
- `location`
	- Best-effort: look for a header containing "Location" and map the matching `columns[index]`.
	- If unknown/missing, return empty string.
- `description`
	- Default to empty string.
	- Optionally map a header containing "Comment" or "Text" if present.

## Error handling (must match contract)

Use the standard error envelope (`ErrorResponse` via `ResponseBuilder`).

- **400 `INVALID_TIMEEDIT_URL`**: missing/blank/malformed `timeeditUrl`
- **502 `TIMEEDIT_UNREACHABLE`**: DNS/timeout/connection errors
- **502 `TIMEEDIT_ERROR_RESPONSE`**: TimeEdit returned non-2xx
- **422 `TIMEEDIT_PARSE_ERROR`**: cannot parse/normalize into DTO

## Local testing

Example (replace URL):

```bash
curl "http://localhost:8080/api/timeedit/schedule?timeeditUrl=https://cloud.timeedit.net/ltu/web/schedule1/...json"
```

What to check:

- `events.length == summary.eventCount`
- `summary.rangeStart` and `summary.rangeEnd` reflect min/max
- No raw upstream JSON fields leak into the response

## Tests

- Resource-level tests should not depend on the real TimeEdit service.
- Use a fixed TimeEdit JSON sample stored locally (captured from a real TimeEdit URL) and assert the normalized output.


