package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.RankingNormalizer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingObjectiveExtractorTest {

    @Nested
    class WithRankingNormalizer {

        private final RankingObjectiveExtractor extractor =
                new RankingObjectiveExtractor(new RankingNormalizer());

        @Test
        void highest_ranked_item_normalizes_to_1() {
            float[][] raw = {{1f}, {3f}, {2f}};
            assertEquals(1.0, extractor.extract(raw).at(1).value(), 1e-9);
        }

        @Test
        void lowest_ranked_item_normalizes_to_0() {
            float[][] raw = {{1f}, {3f}, {2f}};
            assertEquals(0.0, extractor.extract(raw).at(0).value(), 1e-9);
        }

        @Test
        void middle_item_normalizes_to_0_point_5() {
            float[][] raw = {{0f}, {10f}, {5f}};
            assertEquals(0.5, extractor.extract(raw).at(2).value(), 1e-9);
        }

        @Test
        void all_equal_scores_normalize_to_0() {
            float[][] raw = {{4f}, {4f}, {4f}};
            var result = extractor.extract(raw);
            for (int i = 0; i < result.size(); i++) {
                assertEquals(0.0, result.at(i).value(), 1e-9);
            }
        }
    }

    @Nested
    class ExtractionMechanics {

        @Test
        void flattens_first_element_of_each_row_before_normalizing() {
            var extractor = new RankingObjectiveExtractor(scores -> {
                double[] result = new double[scores.length];
                for (int i = 0; i < scores.length; i++) result[i] = scores[i];
                return result;
            });
            float[][] raw = {{3f}, {1f}, {2f}};
            var predictions = extractor.extract(raw);

            assertEquals(3.0, predictions.at(0).value(), 1e-9);
            assertEquals(1.0, predictions.at(1).value(), 1e-9);
            assertEquals(2.0, predictions.at(2).value(), 1e-9);
        }

        @Test
        void result_size_matches_input_row_count() {
            var extractor = new RankingObjectiveExtractor(new RankingNormalizer());
            assertEquals(4, extractor.extract(new float[][]{{1f}, {2f}, {3f}, {4f}}).size());
        }
    }
}