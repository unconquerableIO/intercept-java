package io.unconquerable.intercept.xgboost.prediction.decoder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiSoftProbObjectiveDecoderTest {

    @Nested
    class Decoding {

        @Test
        void decodes_probability_at_target_class_index() {
            var decoder = new MultiSoftProbObjectiveDecoder(1);
            float[][] raw = {{0.3f, 0.7f}};
            assertEquals(0.7, decoder.decode(raw).at(0).value(), 1e-6);
        }

        @Test
        void decodes_correct_index_for_each_row() {
            var decoder = new MultiSoftProbObjectiveDecoder(2);
            float[][] raw = {
                    {0.1f, 0.3f, 0.6f},
                    {0.5f, 0.2f, 0.3f}
            };
            var result = decoder.decode(raw);

            assertEquals(0.6, result.at(0).value(), 1e-6);
            assertEquals(0.3, result.at(1).value(), 1e-6);
        }

        @Test
        void target_class_index_zero_decodes_first_column() {
            var decoder = new MultiSoftProbObjectiveDecoder(0);
            float[][] raw = {{0.9f, 0.1f}};
            assertEquals(0.9, decoder.decode(raw).at(0).value(), 1e-6);
        }

        @Test
        void result_size_matches_input_row_count() {
            var decoder = new MultiSoftProbObjectiveDecoder(0);
            float[][] raw = {{0.5f, 0.5f}, {0.8f, 0.2f}, {0.3f, 0.7f}};
            assertEquals(3, decoder.decode(raw).size());
        }
    }
}