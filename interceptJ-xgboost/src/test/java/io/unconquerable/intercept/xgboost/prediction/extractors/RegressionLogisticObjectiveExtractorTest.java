package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.SigmoidNormalizer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegressionLogisticObjectiveExtractorTest {

    @Nested
    class WithSigmoidNormalizer {

        private final RegressionLogisticObjectiveExtractor extractor =
                new RegressionLogisticObjectiveExtractor(new SigmoidNormalizer());

        @Test
        void log_odds_of_zero_maps_to_probability_of_0_point_5() {
            assertEquals(0.5, extractor.extract(new float[][]{{0f}}).at(0).value(), 1e-9);
        }

        @Test
        void positive_log_odds_maps_to_probability_above_0_point_5() {
            assertTrue(extractor.extract(new float[][]{{3f}}).at(0).value() > 0.5);
        }

        @Test
        void negative_log_odds_maps_to_probability_below_0_point_5() {
            assertTrue(extractor.extract(new float[][]{{-3f}}).at(0).value() < 0.5);
        }

        @Test
        void output_is_in_open_interval_0_1() {
            double value = extractor.extract(new float[][]{{2f}}).at(0).value();
            assertTrue(value > 0.0 && value < 1.0);
        }
    }

    @Nested
    class ExtractionMechanics {

        private final RegressionLogisticObjectiveExtractor extractor =
                new RegressionLogisticObjectiveExtractor(v -> (double) v);

        @Test
        void normalizer_receives_first_element_of_each_row() {
            var result = extractor.extract(new float[][]{{6f}, {2f}});
            assertEquals(6.0, result.at(0).value(), 1e-9);
            assertEquals(2.0, result.at(1).value(), 1e-9);
        }

        @Test
        void result_size_matches_input_row_count() {
            assertEquals(3, extractor.extract(new float[][]{{1f}, {2f}, {3f}}).size());
        }
    }
}