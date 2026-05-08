package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.normalizer.MinMaxNormalizer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegressionSquaredErrorObjectiveDecoderTest {

    @Nested
    class WithMinMaxNormalizer {

        private final RegressionSquaredErrorObjectiveDecoder decoder =
                new RegressionSquaredErrorObjectiveDecoder(new MinMaxNormalizer(0.0, 10.0));

        @Test
        void minimum_raw_value_normalizes_to_0() {
            assertEquals(0.0, decoder.decode(new float[][]{{0f}}).at(0).value(), 1e-9);
        }

        @Test
        void maximum_raw_value_normalizes_to_1() {
            assertEquals(1.0, decoder.decode(new float[][]{{10f}}).at(0).value(), 1e-9);
        }

        @Test
        void midpoint_raw_value_normalizes_to_0_point_5() {
            assertEquals(0.5, decoder.decode(new float[][]{{5f}}).at(0).value(), 1e-9);
        }
    }

    @Nested
    class DecodingMechanics {

        private final RegressionSquaredErrorObjectiveDecoder decoder =
                new RegressionSquaredErrorObjectiveDecoder(v -> (double) v);

        @Test
        void normalizer_receives_first_element_of_each_row() {
            var result = decoder.decode(new float[][]{{3f}, {7f}});
            assertEquals(3.0, result.at(0).value(), 1e-9);
            assertEquals(7.0, result.at(1).value(), 1e-9);
        }

        @Test
        void result_size_matches_input_row_count() {
            assertEquals(3, decoder.decode(new float[][]{{1f}, {2f}, {3f}}).size());
        }
    }
}