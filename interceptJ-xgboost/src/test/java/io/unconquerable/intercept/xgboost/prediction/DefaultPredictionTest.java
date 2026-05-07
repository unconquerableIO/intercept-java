package io.unconquerable.intercept.xgboost.prediction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPredictionTest {

    @Nested
    class Value {

        @Test
        void returns_the_wrapped_double_value() {
            assertEquals(0.87, new DefaultPrediction<>(0.87).value(), 1e-9);
        }

        @Test
        void returns_the_wrapped_integer_value() {
            assertEquals(1, new DefaultPrediction<>(1).value());
        }

        @Test
        void returns_the_wrapped_string_value() {
            assertEquals("fraud", new DefaultPrediction<>("fraud").value());
        }
    }

    @Nested
    class Equality {

        @Test
        void two_instances_with_same_value_are_equal() {
            assertEquals(new DefaultPrediction<>(0.5), new DefaultPrediction<>(0.5));
        }

        @Test
        void two_instances_with_different_values_are_not_equal() {
            assertNotEquals(new DefaultPrediction<>(0.5), new DefaultPrediction<>(0.9));
        }

        @Test
        void instance_is_equal_to_itself() {
            var prediction = new DefaultPrediction<>(0.75);
            assertEquals(prediction, prediction);
        }
    }

    @Nested
    class PredictionContract {

        @Test
        void implements_prediction_interface() {
            assertInstanceOf(Prediction.class, new DefaultPrediction<>(1.0));
        }

        @Test
        void value_from_interface_matches_record_accessor() {
            Prediction<Double> prediction = new DefaultPrediction<>(0.6);
            assertEquals(0.6, prediction.value(), 1e-9);
        }
    }
}