package com.pulse.integration.timeedit.dto;

import java.time.OffsetDateTime;

public class TimeEditSummaryDTO {

    private int eventCount;
    private OffsetDateTime rangeStart;
    private OffsetDateTime rangeEnd;

    public TimeEditSummaryDTO() {
    }

    public TimeEditSummaryDTO(int eventCount, OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        this.eventCount = eventCount;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public OffsetDateTime getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(OffsetDateTime rangeStart) {
        this.rangeStart = rangeStart;
    }

    public OffsetDateTime getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(OffsetDateTime rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
}
