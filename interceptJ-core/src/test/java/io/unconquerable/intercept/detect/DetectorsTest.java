package io.unconquerable.intercept.detect;

import org.junit.jupiter.api.Test;

import static io.unconquerable.intercept.detect.DetectedStatus.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class DetectorsTest {

    private static final Detector<String> STUB = new Detector<>() {
        @Override public String name() { return "stub"; }
        @Override public DetectedStatus detect(String target) {
            return new DetectedStatus(name(), DETECTED);
        }
    };

    @Test
    void detector_factory_returns_a_conditional_detector_builder() {
        var builder = Detectors.detector(STUB);
        assertNotNull(builder);
    }

    @Test
    void built_detector_without_condition_always_runs() {
        // No .when() call — default condition is always true
        var detector = Detectors.detector(STUB).build();
        var result = (DetectedStatus) detector.detect("input");
        assertEquals(DETECTED, result.status());
    }

    @Test
    void built_detector_preserves_wrapped_detector_name() {
        var detector = Detectors.detector(STUB).build();
        assertEquals("stub", detector.name());
    }

    @Test
    void built_detector_skips_when_condition_is_false() {
        var detector = Detectors.detector(STUB).when(() -> false).build();
        var result = (DetectedStatus) detector.detect("input");
        assertEquals(SKIPPED, result.status());
    }

    @Test
    void built_detector_runs_when_condition_is_true() {
        var detector = Detectors.detector(STUB).when(() -> true).build();
        var result = (DetectedStatus) detector.detect("input");
        assertEquals(DETECTED, result.status());
    }
}