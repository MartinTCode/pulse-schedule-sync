package com.pulse.service;

import com.pulse.integration.canvas.CanvasClient;
import com.pulse.integration.canvas.CanvasPublishValidator;
import com.pulse.integration.canvas.CanvasUpstreamException;
import com.pulse.integration.canvas.dto.CanvasCalendarEventRequest;
import com.pulse.integration.canvas.dto.CanvasCalendarEventResponse;
import com.pulse.server.dto.CanvasPublishRequest;
import com.pulse.server.dto.Failure;
import com.pulse.server.dto.PublishResult;
import com.pulse.server.dto.PublishScheduleEvent;

import java.util.ArrayList;
import java.util.List;

public class CanvasPublishService {

    private final CanvasClient canvasClient;

    public CanvasPublishService(CanvasClient canvasClient) {
        this.canvasClient = canvasClient;
    }

    /**
     * Publishes the given canvas publish request.
     * @param request the canvas publish request
     * @return the publish result
     */
    public PublishResult publish(CanvasPublishRequest request) {
        // Validation
        CanvasPublishValidator.validate(request);

        // Loop events
        int published = 0;
        List<Failure> failures = new ArrayList<>();

        String canvasContext = request.getCanvasContext();

        // For each event to publish
        for (PublishScheduleEvent event : request.getSchedule().getEvents()) {
            CanvasCalendarEventRequest canvasReq = toCanvasRequest(canvasContext, event);

            // Call Canvas API to create event
            CanvasClient.CanvasResponse<CanvasCalendarEventResponse> res =
                    canvasClient.createCalendarEvent(canvasReq);

            if (res.isSuccess()) {
                published++;
                continue;
            }

            String code = res.getErrorCode();
            String msg = res.getErrorMessage() != null ? res.getErrorMessage() : code;

            // Fatal errors: throw exception to abort entire publish process
            if ("CONFIG_ERROR".equals(code) ||
                "CANVAS_UNAUTHORIZED".equals(code) ||
                "CANVAS_UNREACHABLE".equals(code) ||
                "CANVAS_ERROR_RESPONSE".equals(code)) {
                throw new CanvasUpstreamException(code, msg);
            }

            // Any other error: record failure and continue
            failures.add(new Failure(event.getExternalId(), msg));
        }

        return new PublishResult(published, failures);
    }

    private CanvasCalendarEventRequest toCanvasRequest(String canvasContext, PublishScheduleEvent event) {
        CanvasCalendarEventRequest req = new CanvasCalendarEventRequest();
        req.setContextCode(canvasContext);
        req.setTitle(event.getTitle());
        req.setStartAt(event.getStart());
        req.setEndAt(event.getEnd());
        req.setLocationName(event.getLocation());
        req.setDescription(event.getDescription());
        return req;
    }
}

