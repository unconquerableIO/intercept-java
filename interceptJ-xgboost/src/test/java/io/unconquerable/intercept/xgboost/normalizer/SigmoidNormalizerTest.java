package io.unconquerable.intercept.xgboost.normalizer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SigmoidNormalizerTest {

    private final SigmoidNormalizer normalizer = new SigmoidNormalizer();

    @Nested
    class KnownValues {

        @Test
        void zero_maps_to_0_point_5() {
            assertEquals(0.5, normalizer.normalize(0f), 1e-9);
        }

        @Test
        void large_positive_value_approaches_1() {
            double result = normalizer.normalize(100f);
            assertEquals(1.0, result, 1e-9);
        }

        @Test
        void large_negative_value_approaches_0() {
            double result = normalizer.normalize(-100f);
            assertEquals(0.0, result, 1e-9);
        }
    }

    @Nested
    class OutputRange {

        @Test
        void positive_input_produces_probability_above_0_point_5() {
            assertTrue(normalizer.normalize(2f) > 0.5);
        }

        @Test
        void negative_input_produces_probability_below_0_point_5() {
            assertTrue(normalizer.normalize(-2f) < 0.5);
        }

        @Test
        void output_is_strictly_between_0_and_1_for_finite_inputs() {
            for (float x : new float[]{-10f, -1f, 0f, 1f, 10f}) {
                double result = normalizer.normalize(x);
                assertTrue(result > 0.0 && result < 1.0,
                        "Expected output in (0,1) for input " + x + " but got " + result);
            }
        }
    }

    @Nested
    class Symmetry {

        @Test
        void sigmoid_is_symmetric_around_0_point_5() {
            float x = 3f;
            double pos = normalizer.normalize(x);
            double neg = normalizer.normalize(-x);
            assertEquals(1.0, pos + neg, 1e-9);
        }
    }
}