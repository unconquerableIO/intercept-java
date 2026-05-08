package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.prediction.Predictions;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryLogisticObjectiveDecoderTest {

    private final BinaryLogisticObjectiveDecoder decoder = new BinaryLogisticObjectiveDecoder();

    @Nested
    class Decoding {

        @Test
        void single_row_produces_one_prediction() {
            Predictions<Double, DefaultPrediction<Double>> result = decoder.decode(new float[][]{{0.8f}});
            assertEquals(1, result.size());
        }

        @Test
        void probability_is_widened_from_float_to_double() {
            double value = decoder.decode(new float[][]{{0.75f}}).at(0).value();
            assertEquals(0.75, value, 1e-6);
        }

        @Test
        void multiple_rows_produce_correct_count() {
            float[][] raw = {{0.1f}, {0.5f}, {0.9f}};
            assertEquals(3, decoder.decode(raw).size());
        }

        @Test
        void each_row_first_element_is_decoded_in_order() {
            float[][] raw = {{0.2f}, {0.6f}, {0.9f}};
            Predictions<Double, DefaultPrediction<Double>> result = decoder.decode(raw);

            assertEquals(0.2, result.at(0).value(), 1e-6);
            assertEquals(0.6, result.at(1).value(), 1e-6);
            assertEquals(0.9, result.at(2).value(), 1e-6);
        }

        @Test
        void boundary_values_are_preserved() {
            float[][] raw = {{0.0f}, {1.0f}};
            Predictions<Double, DefaultPrediction<Double>> result = decoder.decode(raw);

            assertEquals(0.0, result.at(0).value(), 1e-9);
            assertEquals(1.0, result.at(1).value(), 1e-9);
        }
    }
}