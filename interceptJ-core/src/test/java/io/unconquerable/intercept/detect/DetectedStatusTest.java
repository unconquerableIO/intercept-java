package io.unconquerable.intercept.detect;

import org.junit.jupiter.api.Test;

import static io.unconquerable.intercept.detect.DetectedStatus.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class DetectedStatusTest {

    @Test
    void stores_detector_name() {
        var status = new DetectedStatus<>("device-fp", "device-xyz", DETECTED);
        assertEquals("device-fp", status.detectorName());
    }

    @Test
    void stores_target() {
        var status = new DetectedStatus<>("d", "device-xyz", DETECTED);
        assertEquals("device-xyz", status.target());
    }

    @Test
    void stores_status() {
        assertEquals(DETECTED,     new DetectedStatus<>("d", "t", DETECTED).status());
        assertEquals(NOT_DETECTED, new DetectedStatus<>("d", "t", NOT_DETECTED).status());
        assertEquals(SKIPPED,      new DetectedStatus<>("d", "t", SKIPPED).status());
    }

    @Test
    void all_three_status_values_are_distinct() {
        assertNotEquals(DETECTED, NOT_DETECTED);
        assertNotEquals(DETECTED, SKIPPED);
        assertNotEquals(NOT_DETECTED, SKIPPED);
    }

    @Test
    void two_instances_with_same_values_are_equal() {
        var a = new DetectedStatus<>("detector", "device-xyz", DETECTED);
        var b = new DetectedStatus<>("detector", "device-xyz", DETECTED);
        assertEquals(a, b);
    }

    @Test
    void two_instances_with_different_statuses_are_not_equal() {
        var a = new DetectedStatus<>("detector", "device-xyz", DETECTED);
        var b = new DetectedStatus<>("detector", "device-xyz", NOT_DETECTED);
        assertNotEquals(a, b);
    }

    @Test
    void two_instances_with_different_names_are_not_equal() {
        var a = new DetectedStatus<>("detector-a", "device-xyz", DETECTED);
        var b = new DetectedStatus<>("detector-b", "device-xyz", DETECTED);
        assertNotEquals(a, b);
    }

    @Test
    void two_instances_with_different_targets_are_not_equal() {
        var a = new DetectedStatus<>("detector", "device-a", DETECTED);
        var b = new DetectedStatus<>("detector", "device-b", DETECTED);
        assertNotEquals(a, b);
    }
}