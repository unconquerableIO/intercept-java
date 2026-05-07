package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.MinMaxNormalizer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegressionAbsoluteErrorObjectiveExtractorTest {

    @Nested
    class WithMinMaxNormalizer {

        private final RegressionAbsoluteErrorObjectiveExtractor extractor =
                new RegressionAbsoluteErrorObjectiveExtractor(new MinMaxNormalizer(0.0, 100.0));

        @Test
        void minimum_raw_value_normalizes_to_0() {
            assertEquals(0.0, extractor.extract(new float[][]{{0f}}).at(0).value(), 1e-9);
        }

        @Test
        void maximum_raw_value_normalizes_to_1() {
            assertEquals(1.0, extractor.extract(new float[][]{{100f}}).at(0).value(), 1e-9);
        }

        @Test
        void value_above_max_is_clamped_to_1() {
            assertEquals(1.0, extractor.extract(new float[][]{{200f}}).at(0).value(), 1e-9);
        }
    }

    @Nested
    class ExtractionMechanics {

        private final RegressionAbsoluteErrorObjectiveExtractor extractor =
                new RegressionAbsoluteErrorObjectiveExtractor(v -> (double) v);

        @Test
        void normalizer_receives_first_element_of_each_row() {
            var result = extractor.extract(new float[][]{{5f}, {9f}});
            assertEquals(5.0, result.at(0).value(), 1e-9);
            assertEquals(9.0, result.at(1).value(), 1e-9);
        }

        @Test
        void result_size_matches_input_row_count() {
            assertEquals(2, extractor.extract(new float[][]{{1f}, {2f}}).size());
        }
    }
}