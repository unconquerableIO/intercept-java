package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.normalizer.SigmoidNormalizer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.unconquerable.intercept.xgboost.prediction.decoder.Decoders.regressionLogistic;
import static org.junit.jupiter.api.Assertions.*;

class RegressionLogisticObjectiveDecoderTest {

    @Nested
    class WithSigmoidNormalizer {

        private final PredictionsDecoder<Double, DefaultPrediction<Double>> decoder =
                regressionLogistic(new SigmoidNormalizer());

        @Test
        void log_odds_of_zero_maps_to_probability_of_0_point_5() {
            assertEquals(0.5, decoder.decode(new float[][]{{0f}}).at(0).value(), 1e-9);
        }

        @Test
        void positive_log_odds_maps_to_probability_above_0_point_5() {
            assertTrue(decoder.decode(new float[][]{{3f}}).at(0).value() > 0.5);
        }

        @Test
        void negative_log_odds_maps_to_probability_below_0_point_5() {
            assertTrue(decoder.decode(new float[][]{{-3f}}).at(0).value() < 0.5);
        }

        @Test
        void output_is_in_open_interval_0_1() {
            double value = decoder.decode(new float[][]{{2f}}).at(0).value();
            assertTrue(value > 0.0 && value < 1.0);
        }
    }

    @Nested
    class DecodingMechanics {

        private final PredictionsDecoder<Double, DefaultPrediction<Double>> decoder =
                regressionLogistic(v -> (double) v);

        @Test
        void normalizer_receives_first_element_of_each_row() {
            var result = decoder.decode(new float[][]{{6f}, {2f}});
            assertEquals(6.0, result.at(0).value(), 1e-9);
            assertEquals(2.0, result.at(1).value(), 1e-9);
        }

        @Test
        void result_size_matches_input_row_count() {
            assertEquals(3, decoder.decode(new float[][]{{1f}, {2f}, {3f}}).size());
        }
    }
}