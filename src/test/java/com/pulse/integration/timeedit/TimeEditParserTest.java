package com.pulse.integration.timeedit;

import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeEditParserTest {

    @Test
    void parseSchedule_fixture_holidays_parsesEventsAndSummary() throws Exception {
        String rawJson = readResource("/timeedit/sample-holidays.json");
        ZoneId zoneId = ZoneId.systemDefault();

        TimeEditScheduleDTO schedule = TimeEditParser.parseSchedule(rawJson, "https://example.test/timeedit.json", zoneId);

        assertNotNull(schedule);
        assertEquals("TimeEdit", schedule.getSource());
        assertEquals("https://example.test/timeedit.json", schedule.getTimeeditUrl());
        assertNotNull(schedule.getGeneratedAt());

        assertNotNull(schedule.getEvents());
        assertEquals(4, schedule.getEvents().size());

        assertEquals("TE-1106056", schedule.getEvents().get(0).getExternalId());
        assertEquals("Långfredagen", schedule.getEvents().get(0).getTitle());
        assertEquals("", schedule.getEvents().get(0).getLocation());
        assertEquals("", schedule.getEvents().get(0).getDescription());

        assertEquals(
                ZonedDateTime.of(LocalDateTime.of(2026, 4, 3, 8, 0), zoneId).toOffsetDateTime(),
                schedule.getEvents().get(0).getStart()
        );
        assertEquals(
                ZonedDateTime.of(LocalDateTime.of(2026, 4, 4, 8, 0), zoneId).toOffsetDateTime(),
                schedule.getEvents().get(0).getEnd()
        );

        assertNotNull(schedule.getSummary());
        assertEquals(4, schedule.getSummary().getEventCount());
        assertEquals(
                ZonedDateTime.of(LocalDateTime.of(2026, 4, 3, 8, 0), zoneId).toOffsetDateTime(),
                schedule.getSummary().getRangeStart()
        );
        assertEquals(
                ZonedDateTime.of(LocalDateTime.of(2026, 5, 15, 8, 0), zoneId).toOffsetDateTime(),
                schedule.getSummary().getRangeEnd()
        );
    }

    private static String readResource(String classpathResource) throws Exception {
        try (InputStream in = TimeEditParserTest.class.getResourceAsStream(classpathResource)) {
            assertNotNull(in, "Missing test resource: " + classpathResource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
