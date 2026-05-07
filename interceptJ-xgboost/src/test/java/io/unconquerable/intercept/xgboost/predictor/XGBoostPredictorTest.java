package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.functional.Either;
import io.unconquerable.intercept.xgboost.model.DummyModelFactory;
import io.unconquerable.intercept.xgboost.model.FileModelLoader;
import io.unconquerable.intercept.xgboost.model.ModelLoaderException;
import io.unconquerable.intercept.xgboost.model.ModelSource;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.PredictionError;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XGBoostPredictorTest {

    private static XGBoostPredictor predictor;

    @BeforeAll
    static void loadModel() throws Exception {
        Path modelPath = DummyModelFactory.createBinaryClassifierModel();
        var source = new ModelSource(modelPath.toString(), "dummy", "1.0.0");
        predictor = new XGBoostPredictor(new FileModelLoader(source));
    }

    // ========================================================================

    @Nested
    class Construction {

        @Test
        void succeeds_with_valid_model_loader() {
            assertNotNull(predictor);
        }

        @Test
        void propagates_model_loader_exception_when_file_does_not_exist() {
            var source = new ModelSource("/nonexistent/model.ubj", "dummy", "1.0.0");
            assertThrows(ModelLoaderException.class,
                    () -> new XGBoostPredictor(new FileModelLoader(source)));
        }
    }

    // ========================================================================

    @Nested
    class Predict {

        // dummy model was trained with 2 features
        @SuppressWarnings("all")
        private DMatrix matrix(float... values) throws Exception {
            return new DMatrix(values, 1, values.length, Float.NaN);
        }

        @Test
        void returns_left_for_valid_matrix() throws Exception {
            var result = predictor.predict(matrix(1f, 2f));
            assertTrue(result.isLeft());
        }

        @Test
        void left_value_is_a_default_prediction() throws Exception {
            var result = predictor.predict(matrix(1f, 2f));
            assertInstanceOf(DefaultPrediction.class,
                    ((Either.Left<?, ?>) result).value());
        }

        @Test
        void prediction_contains_float_matrix_output() throws Exception {
            var result = predictor.predict(matrix(1f, 2f));
            var prediction = (DefaultPrediction<?>) ((Either.Left<?, ?>) result).value();
            assertInstanceOf(float[][].class, prediction.value());
        }

        @Test
        void binary_classifier_produces_one_score_per_row() throws Exception {
            var result = predictor.predict(matrix(1f, 2f));
            var raw = (float[][]) ((DefaultPrediction<?>) ((Either.Left<?, ?>) result).value()).value();
            assertEquals(1, raw.length);
            assertEquals(1, raw[0].length);
        }

        @Test
        void score_is_a_probability_in_0_1_range() throws Exception {
            var result = predictor.predict(matrix(1f, 2f));
            var raw = (float[][]) ((DefaultPrediction<?>) ((Either.Left<?, ?>) result).value()).value();
            float score = raw[0][0];
            assertTrue(score >= 0f && score <= 1f,
                    "Expected score in [0,1] but got " + score);
        }

        @Test
        void returns_right_when_prediction_fails() {
            // null matrix triggers an exception inside booster.predict, caught and wrapped
            var result = predictor.predict(null);
            assertTrue(result.isRight());
        }

        @Test
        void right_value_is_a_prediction_error() {
            var result = predictor.predict(null);
            assertInstanceOf(PredictionError.class,
                    ((Either.Right<?, ?>) result).value());
        }

        @Test
        void prediction_error_has_non_null_message() {
            var result = predictor.predict(null);
            var error = (PredictionError) ((Either.Right<?, ?>) result).value();
            assertNotNull(error.message());
        }
    }
}