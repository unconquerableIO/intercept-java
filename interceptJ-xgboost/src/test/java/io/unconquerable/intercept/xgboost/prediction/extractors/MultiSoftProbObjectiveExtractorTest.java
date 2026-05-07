package io.unconquerable.intercept.xgboost.prediction.extractors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiSoftProbObjectiveExtractorTest {

    @Nested
    class Extraction {

        @Test
        void extracts_probability_at_target_class_index() {
            var extractor = new MultiSoftProbObjectiveExtractor(1);
            float[][] raw = {{0.3f, 0.7f}};
            assertEquals(0.7, extractor.extract(raw).at(0).value(), 1e-6);
        }

        @Test
        void extracts_correct_index_for_each_row() {
            var extractor = new MultiSoftProbObjectiveExtractor(2);
            float[][] raw = {
                    {0.1f, 0.3f, 0.6f},
                    {0.5f, 0.2f, 0.3f}
            };
            var result = extractor.extract(raw);

            assertEquals(0.6, result.at(0).value(), 1e-6);
            assertEquals(0.3, result.at(1).value(), 1e-6);
        }

        @Test
        void target_class_index_zero_extracts_first_column() {
            var extractor = new MultiSoftProbObjectiveExtractor(0);
            float[][] raw = {{0.9f, 0.1f}};
            assertEquals(0.9, extractor.extract(raw).at(0).value(), 1e-6);
        }

        @Test
        void result_size_matches_input_row_count() {
            var extractor = new MultiSoftProbObjectiveExtractor(0);
            float[][] raw = {{0.5f, 0.5f}, {0.8f, 0.2f}, {0.3f, 0.7f}};
            assertEquals(3, extractor.extract(raw).size());
        }
    }
}