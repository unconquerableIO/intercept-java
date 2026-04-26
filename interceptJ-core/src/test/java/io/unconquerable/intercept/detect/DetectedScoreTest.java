package io.unconquerable.intercept.detect;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DetectedScoreTest {

    @Test
    void stores_detector_name() {
        var score = new DetectedScore("ip-reputation", BigDecimal.ONE);
        assertEquals("ip-reputation", score.detectorName());
    }

    @Test
    void stores_score() {
        var value = new BigDecimal("0.87");
        var score = new DetectedScore("scorer", value);
        assertEquals(value, score.score());
    }

    @Test
    void works_with_zero_score() {
        var score = new DetectedScore("scorer", BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, score.score());
    }

    @Test
    void works_with_score_greater_than_one() {
        var value = new BigDecimal("999.99");
        var score = new DetectedScore("scorer", value);
        assertEquals(value, score.score());
    }

    @Test
    void two_instances_with_same_values_are_equal() {
        var a = new DetectedScore("scorer", new BigDecimal("0.5"));
        var b = new DetectedScore("scorer", new BigDecimal("0.5"));
        assertEquals(a, b);
    }

    @Test
    void two_instances_with_different_names_are_not_equal() {
        var a = new DetectedScore("scorer-a", new BigDecimal("0.5"));
        var b = new DetectedScore("scorer-b", new BigDecimal("0.5"));
        assertNotEquals(a, b);
    }

    @Test
    void two_instances_with_different_scores_are_not_equal() {
        var a = new DetectedScore("scorer", new BigDecimal("0.5"));
        var b = new DetectedScore("scorer", new BigDecimal("0.9"));
        assertNotEquals(a, b);
    }
}