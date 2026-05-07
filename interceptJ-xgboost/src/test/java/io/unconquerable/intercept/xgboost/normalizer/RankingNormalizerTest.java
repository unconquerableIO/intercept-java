package io.unconquerable.intercept.xgboost.normalizer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingNormalizerTest {

    private final RankingNormalizer normalizer = new RankingNormalizer();

    @Nested
    class BatchNormalization {

        @Test
        void highest_score_normalizes_to_1() {
            double[] result = normalizer.normalize(new float[]{1f, 2f, 3f});
            assertEquals(1.0, result[2], 1e-9);
        }

        @Test
        void lowest_score_normalizes_to_0() {
            double[] result = normalizer.normalize(new float[]{1f, 2f, 3f});
            assertEquals(0.0, result[0], 1e-9);
        }

        @Test
        void midpoint_score_normalizes_to_0_point_5() {
            double[] result = normalizer.normalize(new float[]{0f, 5f, 10f});
            assertEquals(0.5, result[1], 1e-9);
        }

        @Test
        void result_length_matches_input_length() {
            float[] input = {3f, 1f, 4f, 1f, 5f};
            assertEquals(input.length, normalizer.normalize(input).length);
        }

        @Test
        void relative_order_is_preserved() {
            double[] result = normalizer.normalize(new float[]{10f, 30f, 20f});
            assertTrue(result[0] < result[2]);
            assertTrue(result[2] < result[1]);
        }

        @Test
        void all_values_within_0_and_1() {
            double[] result = normalizer.normalize(new float[]{5f, 15f, 10f, 20f, 1f});
            for (double v : result) {
                assertTrue(v >= 0.0 && v <= 1.0, "Expected value in [0,1] but got " + v);
            }
        }
    }

    @Nested
    class DegenerateCases {

        @Test
        void single_element_array_normalizes_to_0() {
            double[] result = normalizer.normalize(new float[]{7f});
            assertEquals(0.0, result[0], 1e-9);
        }

        @Test
        void all_equal_scores_normalize_to_0() {
            double[] result = normalizer.normalize(new float[]{4f, 4f, 4f});
            for (double v : result) {
                assertEquals(0.0, v, 1e-9);
            }
        }

        @Test
        void empty_array_returns_empty_result() {
            assertEquals(0, normalizer.normalize(new float[]{}).length);
        }
    }
}