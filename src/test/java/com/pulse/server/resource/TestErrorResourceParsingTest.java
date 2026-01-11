package com.pulse.server.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrorResourceParsingTest {

    @Test
    public void extractEventCount_readsFromReservationsArray() {
        String json = "{\"reservations\":[{},{},{}]}";
        assertEquals(3, TestErrorResource.extractEventCountFromTimeEditJson(json));
    }

    @Test
    public void extractEventCount_readsFromInfoReservationCount() {
        String json = "{\"info\":{\"reservationcount\":4}}";
        assertEquals(4, TestErrorResource.extractEventCountFromTimeEditJson(json));
    }

    @Test
    public void extractEventCount_returnsNullOnInvalidJson() {
        assertNull(TestErrorResource.extractEventCountFromTimeEditJson("not-json"));
    }
}
