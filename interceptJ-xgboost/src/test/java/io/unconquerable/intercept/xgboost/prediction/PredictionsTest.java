package io.unconquerable.intercept.xgboost.prediction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PredictionsTest {

    private static Predictions<Double, DefaultPrediction<Double>> of(double... values) {
        List<DefaultPrediction<Double>> list = new java.util.ArrayList<>();
        for (double v : values) list.add(new DefaultPrediction<>(v));
        return new Predictions<>(list);
    }

    @Nested
    class At {

        @Test
        void returns_prediction_at_correct_index() {
            var predictions = of(0.1, 0.5, 0.9);
            assertEquals(0.1, predictions.at(0).value(), 1e-9);
            assertEquals(0.5, predictions.at(1).value(), 1e-9);
            assertEquals(0.9, predictions.at(2).value(), 1e-9);
        }

        @Test
        void throws_for_out_of_bounds_index() {
            var predictions = of(0.5);
            assertThrows(IndexOutOfBoundsException.class, () -> predictions.at(1));
        }
    }

    @Nested
    class Size {

        @Test
        void returns_correct_count_for_non_empty_batch() {
            assertEquals(3, of(0.1, 0.2, 0.3).size());
        }

        @Test
        void returns_zero_for_empty_batch() {
            assertEquals(0, new Predictions<>(List.of()).size());
        }
    }

    @Nested
    class All {

        @Test
        void returns_all_predictions_in_insertion_order() {
            var predictions = of(0.2, 0.7, 0.4);
            var all = predictions.all();

            assertEquals(0.2, all.get(0).value(), 1e-9);
            assertEquals(0.7, all.get(1).value(), 1e-9);
            assertEquals(0.4, all.get(2).value(), 1e-9);
        }

        @Test
        @SuppressWarnings("all")
        void returned_list_is_unmodifiable() {
            var predictions = of(0.5);
            assertThrows(UnsupportedOperationException.class,
                    () -> predictions.all().add(new DefaultPrediction<>(0.9)));
        }

        @Test
        void size_of_all_matches_size() {
            var predictions = of(0.1, 0.9);
            assertEquals(predictions.size(), predictions.all().size());
        }
    }
}