package io.unconquerable.intercept.detect;

import org.junit.jupiter.api.Test;

import static io.unconquerable.intercept.detect.DetectedStatus.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class DetectedStatusTest {

    @Test
    void stores_detector_name() {
        var status = new DetectedStatus("device-fp", DETECTED);
        assertEquals("device-fp", status.detectorName());
    }

    @Test
    void stores_status() {
        assertEquals(DETECTED,     new DetectedStatus("d", DETECTED).status());
        assertEquals(NOT_DETECTED, new DetectedStatus("d", NOT_DETECTED).status());
        assertEquals(SKIPPED,      new DetectedStatus("d", SKIPPED).status());
    }

    @Test
    void all_three_status_values_are_distinct() {
        assertNotEquals(DETECTED, NOT_DETECTED);
        assertNotEquals(DETECTED, SKIPPED);
        assertNotEquals(NOT_DETECTED, SKIPPED);
    }

    @Test
    void two_instances_with_same_values_are_equal() {
        var a = new DetectedStatus("detector", DETECTED);
        var b = new DetectedStatus("detector", DETECTED);
        assertEquals(a, b);
    }

    @Test
    void two_instances_with_different_statuses_are_not_equal() {
        var a = new DetectedStatus("detector", DETECTED);
        var b = new DetectedStatus("detector", NOT_DETECTED);
        assertNotEquals(a, b);
    }

    @Test
    void two_instances_with_different_names_are_not_equal() {
        var a = new DetectedStatus("detector-a", DETECTED);
        var b = new DetectedStatus("detector-b", DETECTED);
        assertNotEquals(a, b);
    }
}