package com.pulse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(CanvasPublishService.class);

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
        logger.info("Starting publish operation");
        logger.debug("Validating request");
        
        // Validation
        CanvasPublishValidator.validate(request);
        logger.debug("Request validation passed");

        // Loop events
        int published = 0;
        List<Failure> failures = new ArrayList<>();

        String canvasContext = request.getCanvasContext();
        int totalEvents = request.getSchedule().getEvents().size();
        logger.info("Processing {} events for Canvas context: {}", totalEvents, canvasContext);

        // For each event to publish
        for (PublishScheduleEvent event : request.getSchedule().getEvents()) {
            logger.debug("Publishing event: externalId={}, title={}", event.getExternalId(), event.getTitle());
            CanvasCalendarEventRequest canvasReq = toCanvasRequest(canvasContext, event);

            // Call Canvas API to create event
            CanvasClient.CanvasResponse<CanvasCalendarEventResponse> res =
                    canvasClient.createCalendarEvent(canvasReq);

            if (res.isSuccess()) {
                published++;
                logger.debug("Event published successfully: externalId={}, canvasId={}", 
                    event.getExternalId(), 
                    res.getData() != null ? res.getData().getId() : "unknown");
                continue;
            }

            String code = res.getErrorCode();
            String msg = res.getErrorMessage() != null ? res.getErrorMessage() : code;

            logger.warn("Event publish failed: externalId={}, code={}, message={}", 
                event.getExternalId(), code, msg);

            // Fatal errors: throw exception to abort entire publish process
            if ("CONFIG_ERROR".equals(code) ||
                "CANVAS_UNAUTHORIZED".equals(code) ||
                "CANVAS_UNREACHABLE".equals(code) ||
                "CANVAS_ERROR_RESPONSE".equals(code)) {
                logger.error("Fatal error encountered, aborting publish process: code={}", code);
                throw new CanvasUpstreamException(code, msg);
            }

            // Any other error: record failure and continue
            logger.debug("Recording non-fatal failure for event: {}", event.getExternalId());
            failures.add(new Failure(event.getExternalId(), msg));
        }

        logger.info("Publish operation completed: {} published, {} failed out of {} total", 
            published, failures.size(), totalEvents);

        return new PublishResult(published, failures);
    }

    private CanvasCalendarEventRequest toCanvasRequest(String canvasContext, PublishScheduleEvent event) {
        CanvasCalendarEventRequest req = new CanvasCalendarEventRequest();
        req.setContextCode(canvasContext);
        req.setTitle(event.getTitle());
        req.setStartAt(event.getStart() != null ? event.getStart().toString() : null);
        req.setEndAt(event.getEnd() != null ? event.getEnd().toString() : null);
        req.setLocationName(event.getLocation());
        req.setDescription(event.getDescription());
        return req;
    }
}

