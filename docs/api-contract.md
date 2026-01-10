# API Contract - Schedule integration Server

## Service Overview

**Service name:** Schedule Integration API

**Base URI**: http://localhost:8080/
**Format**: JSON (application/json)
**Time format**: ISO-8601 with UTC offset (example: `2026-02-03T08:15:00+01:00`)
**Canvas credentials**: Loaded server-side from .env file (CANVAS_BASE_URL, CANVAS_TOKEN)

The server is **stateless**, it only act as an integration layer between TimeEdit and Canvas.

**Responsibilities:**

- Expose health and diagnostic endpoints to indicate server availability and Canvas credential status.

- Retrieve schedule data from TimeEdit using a client-provided URL, including detection and reporting of upstream fetch and parsing errors.

- Normalize retrieved TimeEdit data into a server-defined schedule DTO and return it to the client.

- Accept a finalized schedule payload from the client and validate it against server-defined rules.

- Map validated schedule events to the Canvas Calendar Events API format.

- Publish schedule events to Canvas using server-side credentials loaded from `.env`.

- Return publication results (success, partial success, or failure) to the client.

- Operate without persistent storage; all schedule state is supplied by the client per request.

## Error format

used for non-2xx responses:

```json
{
    "error": {
        "code": "SOME_ERROR_CODE",
        "message": "Human-readable summary",
        "details": {}
    }
}
```

## Endpoints

### Health

Basic Server Health Check.

**Path (+base):** GET /health

#### Responses

**200 - OK:**  

```json
{ "status": "UP" }
```

### Canvas Authentication Diagnostic

Checks whether the server can authenticate to Canvas using its configured (.env) credentials.

**Path (+base):** GET /health/canvas-auth

#### Responses

**200  - OK:** 

```json
{
 "canvasAuth": "OK",
 "message": "Canvas API connection successful",
 "canvasUser": {
 "id": "<canvas-user-id>",
 "login": "<canvas-login-id>"
 }
}
```

**500 - Missing server configuration:** 

Returned when required environment variables are missing or .env file not found.

```json
{
    "error": {
        "code": "CONFIG_ERROR",
        "message": "Missing CANVAS_TOKEN in .env or system environment",
        "details": {}
    }
}
```

**502 - Canvas Unreachable:** 

Returned when the .env URL is wrong or unresponsive.

```json
{
    "error": {
        "code": "CANVAS_UNREACHABLE",
        "message": "Canvas host could not be reached",
        "details": {}
    }
}
```

**401 - Canvas authentication failed:**

Returned when for example the wrong API key is set up in the .env file.

```json
{
    "error": {
        "code": "CANVAS_UNAUTHORIZED",
        "message": "Canvas API authentication failed (401). Check CANVAS_TOKEN.",
        "details": {}
    }
}
```

### TimeEdit Service

Fetches and normalizes schedule data from TimeEdit.

**Path (+base):** GET /api/timeedit/schedule

**Query parameter (required):** 

- `timeeditUrl` (string): Full TimeEdit schedule URL

#### Responses

**200 - OK:**

```json
{
 "source": "TimeEdit",
 "timeeditUrl": "https://cloud.timeedit.net/.../ri.json",
 "generatedAt": "2026-01-10T12:34:56+01:00",
 "events": [
 {
 "externalId": "TE-983472",
 "title": "Systemvetenskap – Föreläsning",
 "start": "2026-02-03T08:15:00+01:00",
 "end": "2026-02-03T09:45:00+01:00",
 "location": "A109",
 "description": ""
 },
 {
 "externalId": "TE-983473",
 "title": "Systemvetenskap – Laboration",
 "start": "2026-02-03T10:15:00+01:00",
 "end": "2026-02-03T11:45:00+01:00",
 "location": "Lab B214",
 "description": "Ta med dator."
 },
 {
 "externalId": "TE-983510",
 "title": "Systemvetenskap – Seminarie",
 "start": "2026-02-05T13:00:00+01:00",
 "end": "2026-02-05T14:30:00+01:00",
 "location": "Zoom",
 "description": "Länk publiceras i Canvas."
 }
 ],
 "summary": {
 "eventCount": 3,
 "rangeStart": "2026-02-03T08:15:00+01:00",
 "rangeEnd": "2026-02-05T14:30:00+01:00"
 }
}
```

for error responses, summarised below:

- **400 INVALID_TIMEEDIT_URL**: missing/malformed URL

- **502 TIMEEDIT_UNREACHABLE**: timeout/DNS/connection failure

- **502 TIMEEDIT_ERROR_RESPONSE**: TimeEdit returned non-2xx (e.g., 404/500)

- **422 TIMEEDIT_PARSE_ERROR**: cannot parse/normalize response into DTO

### Canvas Service

Publishes a finalized schedule (multiple events) to a Canvas calendar context.
The server maps each event to a Canvas `calendar_events` POST request and submits them one-by-one to Canvas

**Path (+base):** POST /api/canvas/publish

**Request Body (to server):**

```json
{
 "canvasContext": "course_21649",
 "schedule": {
 "source": "TimeEdit",
 "timeeditUrl": "https://cloud.timeedit.net/.../ri.json",
 "events": [
 {
 "externalId": "TE-983472",
 "title": "Systemvetenskap – Föreläsning",
 "start": "2026-02-03T08:15:00+01:00",
 "end": "2026-02-03T09:45:00+01:00",
 "location": "A109",
 "description": ""
 },
 {
 "externalId": "TE-983473",
 "title": "Systemvetenskap – Laboration",
 "start": "2026-02-03T10:15:00+01:00",
 "end": "2026-02-03T11:45:00+01:00",
 "location": "Lab B214",
 "description": "Ta med dator."
 },
 {
 "externalId": "TE-983510",
 "title": "Systemvetenskap – Seminarie",
 "start": "2026-02-05T13:00:00+01:00",
 "end": "2026-02-05T14:30:00+01:00",
 "location": "Zoom",
 "description": "Länk publiceras i Canvas."
 }
 ]
 }
}
```

**Server-side canvas mapping (for context):**

For each of these events, the server performs a POST request to the Canvas API per:

```bash
POST https://canvas.ltu.se/api/v1/calendar_events
Authorization: Bearer <CANVAS_TOKEN>
Content-Type: application/x-www-form-urlencoded
```

with the **request body** being structured per:

```bash
calendar_event[context_code]=course_21649
calendar_event[title]=Systemvetenskap – Föreläsning
calendar_event[start_at]=2026-02-03T08:15:00+01:00
calendar_event[end_at]=2026-02-03T09:45:00+01:00
calendar_event[location_name]=A109
calendar_event[description]=Hybrid lecture. Zoom link in Canvas.
```

#### Responses

**200 - OK:**

```json
{
 "published": 3,
 "failed": 0
}
```

**207 - partial success:**

```json
{
 "published": 2,
 "failed": 1,
 "failures": [
 {
 "externalId": "TE-983473",
 "reason": "CANVAS_VALIDATION_ERROR"
 }
 ]
}
```

for error responses, summarised below:

- **400 INVALID_PUBLISH_REQUEST**: missing required top-level fields

- **422 VALIDATION_ERROR**: event-level validation failure (start/end/title/etc.)

- **500 CONFIG_ERROR**: missing Canvas config in `.env`

- **401 CANVAS_UNAUTHORIZED**: token rejected

- **502 CANVAS_UNREACHABLE**: network/DNS/timeout

- **502 CANVAS_ERROR_RESPONSE**: Canvas returned non-2xx (other than 401)

## Validation Rules (server-side)

- canvasContext field must match course_<id> or user_<id>

- schedule.events field must not be empty

- Each event must have:

        - title

        - start < end

        - ISO-8601 datetime strings

## Example flow (client calls to server)

### Flow A: Normal

1. `GET /health`

2. `GET /api/timeedit/schedule?timeeditUrl=...`

3. `GET /health/canvas-auth`

4. `POST /api/canvas/publish`

### Flow B: TimeEdit fetch failure

- `GET /api/timeedit/schedule?...` → `502 TIMEEDIT_UNREACHABLE`

### Flow C: Canvas credential failure

- `GET /health/canvas-auth` → `401 CANVAS_UNAUTHORIZED`
