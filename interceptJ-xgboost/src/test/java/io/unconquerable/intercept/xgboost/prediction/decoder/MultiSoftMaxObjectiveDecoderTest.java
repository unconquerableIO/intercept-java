package io.unconquerable.intercept.xgboost.prediction.decoder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.unconquerable.intercept.xgboost.prediction.decoder.Decoders.multiSoftMax;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiSoftMaxObjectiveDecoderTest {

    @Nested
    class Matching {

        @Test
        void row_matching_target_class_produces_1() {
            var decoder = multiSoftMax(2);
            assertEquals(1, decoder.decode(new float[][]{{2f}}).at(0).value());
        }

        @Test
        void row_not_matching_target_class_produces_0() {
            var decoder = multiSoftMax(2);
            assertEquals(0, decoder.decode(new float[][]{{1f}}).at(0).value());
        }

        @Test
        void mixed_batch_produces_correct_indicators() {
            var decoder = multiSoftMax(1);
            float[][] raw = {{0f}, {1f}, {2f}, {1f}};
            var result = decoder.decode(raw);

            assertEquals(0, result.at(0).value());
            assertEquals(1, result.at(1).value());
            assertEquals(0, result.at(2).value());
            assertEquals(1, result.at(3).value());
        }

        @Test
        void target_class_index_zero_is_supported() {
            var decoder = multiSoftMax(0);
            assertEquals(1, decoder.decode(new float[][]{{0f}}).at(0).value());
            assertEquals(0, decoder.decode(new float[][]{{3f}}).at(0).value());
        }
    }

    @Nested
    class DecodingMechanics {

        @Test
        void result_size_matches_input_row_count() {
            var decoder = multiSoftMax(0);
            assertEquals(4, decoder.decode(new float[][]{{0f}, {1f}, {0f}, {2f}}).size());
        }
    }
}