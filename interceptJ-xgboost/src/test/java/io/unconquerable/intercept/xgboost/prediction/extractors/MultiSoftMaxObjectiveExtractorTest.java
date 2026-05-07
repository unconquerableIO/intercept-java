package io.unconquerable.intercept.xgboost.prediction.extractors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiSoftMaxObjectiveExtractorTest {

    @Nested
    class Matching {

        @Test
        void row_matching_target_class_produces_1() {
            var extractor = new MultiSoftMaxObjectiveExtractor(2);
            assertEquals(1, extractor.extract(new float[][]{{2f}}).at(0).value());
        }

        @Test
        void row_not_matching_target_class_produces_0() {
            var extractor = new MultiSoftMaxObjectiveExtractor(2);
            assertEquals(0, extractor.extract(new float[][]{{1f}}).at(0).value());
        }

        @Test
        void mixed_batch_produces_correct_indicators() {
            var extractor = new MultiSoftMaxObjectiveExtractor(1);
            float[][] raw = {{0f}, {1f}, {2f}, {1f}};
            var result = extractor.extract(raw);

            assertEquals(0, result.at(0).value());
            assertEquals(1, result.at(1).value());
            assertEquals(0, result.at(2).value());
            assertEquals(1, result.at(3).value());
        }

        @Test
        void target_class_index_zero_is_supported() {
            var extractor = new MultiSoftMaxObjectiveExtractor(0);
            assertEquals(1, extractor.extract(new float[][]{{0f}}).at(0).value());
            assertEquals(0, extractor.extract(new float[][]{{3f}}).at(0).value());
        }
    }

    @Nested
    class ExtractionMechanics {

        @Test
        void result_size_matches_input_row_count() {
            var extractor = new MultiSoftMaxObjectiveExtractor(0);
            assertEquals(4, extractor.extract(new float[][]{{0f}, {1f}, {0f}, {2f}}).size());
        }
    }
}