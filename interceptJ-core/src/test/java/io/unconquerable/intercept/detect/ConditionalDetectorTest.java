package io.unconquerable.intercept.detect;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.unconquerable.intercept.detect.DetectedStatus.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class ConditionalDetectorTest {

    // Fixture: always returns DETECTED
    private static final Detector<String> ALWAYS_DETECTED = new Detector<>() {
        @Override public String name() { return "always-detected"; }
        @Override public DetectedStatus detect(String target) {
            return new DetectedStatus(name(), DETECTED);
        }
    };


    // =========================================================================

    @Nested
    class Delegation {

        @Test
        void name_delegates_to_wrapped_detector() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true)
                    .build();

            assertEquals("always-detected", conditional.name());
        }

        @Test
        void detect_delegates_to_wrapped_detector_when_condition_is_true() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true)
                    .build();

            var result = conditional.detect("input");

            assertInstanceOf(DetectedStatus.class, result);
            assertEquals(DETECTED, ((DetectedStatus) result).status());
        }

        @Test
        void detect_returns_the_exact_result_of_wrapped_detector() {
            var expected = new DetectedScore("always-detected", new BigDecimal("0.75"));
            Detector<String> scorer = new Detector<>() {
                @Override public String name() { return "always-detected"; }
                @Override public DetectedScore detect(String target) { return expected; }
            };

            var conditional = ConditionalDetector.detector(scorer)
                    .when(() -> true)
                    .build();

            assertSame(expected, conditional.detect("input"));
        }

        @Test
        void wrapped_detector_receives_the_target_value() {
            String[] captured = {null};
            Detector<String> capturing = new Detector<>() {
                @Override public String name() { return "capturing"; }
                @Override public DetectedStatus detect(String target) {
                    captured[0] = target;
                    return new DetectedStatus(name(), NOT_DETECTED);
                }
            };

            ConditionalDetector.detector(capturing).when(() -> true).build().detect("my-target");

            assertEquals("my-target", captured[0]);
        }
    }

    // =========================================================================

    @Nested
    class SkipBehaviour {

        @Test
        void returns_skipped_status_when_condition_is_false() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false)
                    .build();

            var result = conditional.detect("input");

            assertInstanceOf(DetectedStatus.class, result);
            assertEquals(SKIPPED, ((DetectedStatus) result).status());
        }

        @Test
        void skipped_result_carries_wrapped_detector_name() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false)
                    .build();

            var result = (DetectedStatus) conditional.detect("input");

            assertEquals("always-detected", result.detectorName());
        }

        @Test
        void wrapped_detector_is_not_called_when_condition_is_false() {
            int[] callCount = {0};
            Detector<String> counting = new Detector<>() {
                @Override public String name() { return "counting"; }
                @Override public DetectedStatus detect(String target) {
                    callCount[0]++;
                    return new DetectedStatus(name(), DETECTED);
                }
            };

            ConditionalDetector.detector(counting).when(() -> false).build().detect("input");

            assertEquals(0, callCount[0]);
        }

        @Test
        void default_condition_is_always_true_when_no_when_is_set() {
            // build() without when() — detector should always run
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED).build();

            var result = (DetectedStatus) conditional.detect("input");

            assertEquals(DETECTED, result.status());
        }
    }

    // =========================================================================

    @Nested
    class ConditionComposition {

        @Test
        void and_runs_detector_when_both_conditions_are_true() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true).and(() -> true)
                    .build();

            assertEquals(DETECTED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void and_skips_detector_when_first_condition_is_false() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false).and(() -> true)
                    .build();

            assertEquals(SKIPPED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void and_skips_detector_when_second_condition_is_false() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true).and(() -> false)
                    .build();

            assertEquals(SKIPPED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void and_short_circuits_and_does_not_evaluate_second_condition_when_first_is_false() {
            boolean[] secondEvaluated = {false};

            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false)
                    .and(() -> { secondEvaluated[0] = true; return true; })
                    .build();

            conditional.detect("x");

            assertFalse(secondEvaluated[0]);
        }

        @Test
        void or_runs_detector_when_first_condition_is_true() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true).or(() -> false)
                    .build();

            assertEquals(DETECTED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void or_runs_detector_when_second_condition_is_true() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false).or(() -> true)
                    .build();

            assertEquals(DETECTED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void or_skips_detector_when_both_conditions_are_false() {
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> false).or(() -> false)
                    .build();

            assertEquals(SKIPPED, ((DetectedStatus) conditional.detect("x")).status());
        }

        @Test
        void or_short_circuits_and_does_not_evaluate_second_condition_when_first_is_true() {
            boolean[] secondEvaluated = {false};

            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true)
                    .or(() -> { secondEvaluated[0] = true; return false; })
                    .build();

            conditional.detect("x");

            assertFalse(secondEvaluated[0]);
        }

        @Test
        void multiple_and_and_or_conditions_are_composed_left_to_right() {
            // (true AND false) OR true  => false OR true => true => runs
            var conditional = ConditionalDetector.detector(ALWAYS_DETECTED)
                    .when(() -> true).and(() -> false).or(() -> true)
                    .build();

            assertEquals(DETECTED, ((DetectedStatus) conditional.detect("x")).status());
        }
    }
}