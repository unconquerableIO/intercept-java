package io.unconquerable.intercept.xgboost.normalizer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinMaxNormalizerTest {

    @Nested
    class NormalRange {

        @Test
        void minimum_value_normalizes_to_0() {
            assertEquals(0.0, new MinMaxNormalizer(0.0, 10.0).normalize(0f), 1e-9);
        }

        @Test
        void maximum_value_normalizes_to_1() {
            assertEquals(1.0, new MinMaxNormalizer(0.0, 10.0).normalize(10f), 1e-9);
        }

        @Test
        void midpoint_normalizes_to_0_point_5() {
            assertEquals(0.5, new MinMaxNormalizer(0.0, 10.0).normalize(5f), 1e-9);
        }

        @Test
        void arbitrary_value_in_range_is_correctly_scaled() {
            assertEquals(0.25, new MinMaxNormalizer(0.0, 8.0).normalize(2f), 1e-9);
        }

        @Test
        void negative_range_is_handled_correctly() {
            assertEquals(0.5, new MinMaxNormalizer(-10.0, 10.0).normalize(0f), 1e-9);
        }
    }

    @Nested
    class Clamping {

        @Test
        void value_below_min_is_clamped_to_0() {
            assertEquals(0.0, new MinMaxNormalizer(2.0, 8.0).normalize(-5f), 1e-9);
        }

        @Test
        void value_above_max_is_clamped_to_1() {
            assertEquals(1.0, new MinMaxNormalizer(2.0, 8.0).normalize(100f), 1e-9);
        }
    }

    @Nested
    class DegenerateRange {

        @Test
        void when_min_equals_max_returns_0() {
            assertEquals(0.0, new MinMaxNormalizer(5.0, 5.0).normalize(5f), 1e-9);
        }

        @Test
        void when_min_equals_max_any_input_returns_0() {
            var normalizer = new MinMaxNormalizer(3.0, 3.0);
            assertEquals(0.0, normalizer.normalize(0f),   1e-9);
            assertEquals(0.0, normalizer.normalize(3f),   1e-9);
            assertEquals(0.0, normalizer.normalize(100f), 1e-9);
        }
    }

    @Nested
    class RecordEquality {

        @Test
        void two_normalizers_with_same_bounds_are_equal() {
            assertEquals(new MinMaxNormalizer(0.0, 1.0), new MinMaxNormalizer(0.0, 1.0));
        }

        @Test
        void normalizers_with_different_bounds_are_not_equal() {
            assertNotEquals(new MinMaxNormalizer(0.0, 1.0), new MinMaxNormalizer(0.0, 2.0));
        }
    }
}