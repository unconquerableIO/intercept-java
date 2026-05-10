package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.xgboost.inference.Decision;
import io.unconquerable.intercept.xgboost.inference.Inference;
import io.unconquerable.intercept.xgboost.model.DummyModelFactory;
import io.unconquerable.intercept.xgboost.model.FileModelLoader;
import io.unconquerable.intercept.xgboost.model.ModelSource;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Outcome;
import io.unconquerable.intercept.xgboost.prediction.PredictionError;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import static io.unconquerable.intercept.xgboost.prediction.decoder.Decoders.binaryLogistic;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static io.unconquerable.intercept.xgboost.predictor.Predictor.predictor;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class PredictorTest {

    // ── Test doubles ─────────────────────────────────────────────────────────

    enum Result implements Decision { PASS, FAIL }

    record TestInference(Result decision) implements Inference<Result> {}

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private static XGBoostPredictor model;

    @BeforeAll
    static void loadModel() throws Exception {
        Path modelPath = DummyModelFactory.createBinaryClassifierModel();
        var source = new ModelSource(modelPath.toString(), "dummy", "1.0.0");
        model = new XGBoostPredictor(new FileModelLoader(source));
    }

    /** Builds a single-row DMatrix from the given 2-feature values. */
    @SuppressWarnings("all")
    private static DMatrix matrix(float f1, float f2) throws Exception {
        return new DMatrix(new float[]{f1, f2}, 1, 2, Float.NaN);
    }

    // ── Full pipeline ─────────────────────────────────────────────────────────

    @Nested
    class FullPipeline {

        @Test
        void success_path_routes_to_success_interpreter() throws Exception {
            Result decision = predictor(model, matrix(1f, 2f))
                    .translate(m -> m)
                    .predict()
                    .decode(binaryLogistic())
                    .interpret(p -> new TestInference(Result.PASS))
                    .orError(e -> new TestInference(Result.FAIL))
                    .decision();

            assertEquals(Result.PASS, decision);
        }

        @Test
        void error_path_routes_to_error_interpreter() {
            Result decision = predictor(model, (DMatrix) null)
                    .translate(m -> m)
                    .predict()
                    .decode(binaryLogistic())
                    .interpret(p -> new TestInference(Result.PASS))
                    .orError(e -> new TestInference(Result.FAIL))
                    .decision();

            assertEquals(Result.FAIL, decision);
        }

        @Test
        void error_interpreter_receives_prediction_error() {
            PredictionError[] captured = new PredictionError[1];

            predictor(model, (DMatrix) null)
                    .translate(m -> m)
                    .predict()
                    .decode(binaryLogistic())
                    .interpret(p -> new TestInference(Result.PASS))
                    .orError(e -> { captured[0] = e; return new TestInference(Result.FAIL); });

            assertNotNull(captured[0]);
            assertNotNull(captured[0].message());
        }

        @Test
        void success_interpreter_receives_decoded_predictions() throws Exception {
            Predictions<?, ?>[] captured = new Predictions[1];

            predictor(model, matrix(1f, 2f))
                    .translate(m -> m)
                    .predict()
                    .decode(binaryLogistic())
                    .interpret(p -> { captured[0] = p; return new TestInference(Result.PASS); })
                    .orError(e -> new TestInference(Result.FAIL));

            assertNotNull(captured[0]);
            assertEquals(1, captured[0].size());
        }

        @Test
        void decoded_prediction_is_a_probability_in_0_1_range() throws Exception {
            double score = predictor(model, matrix(1f, 2f))
                    .translate(m -> m)
                    .predict()
                    .decode(binaryLogistic())
                    .decoded()
                    .fold(p -> p.at(0).value(), e -> -1.0);

            assertTrue(score >= 0.0 && score <= 1.0,
                    "Expected probability in [0, 1] but got " + score);
        }
    }

    // ── No translate ──────────────────────────────────────────────────────────

    @Nested
    class WithoutTranslate {

        @Test
        void predict_without_translate_produces_failure() {
            Outcome<DefaultPrediction<float[][]>, PredictionError> outcome =
                    predictor(model, "any-input")
                            .predict()
                            .outcome();

            assertTrue(outcome.isFailure());
        }

        @Test
        void failure_message_indicates_missing_features() {
            PredictionError error = (PredictionError) ((Outcome.Failure<?, ?>) predictor(model, "any-input")
                    .predict()
                    .outcome()).error();

            assertTrue(error.message().toLowerCase().contains("no features"));
        }

        @Test
        void error_interpreter_is_invoked_when_translate_is_skipped() {
            Result decision = predictor(model, "any-input")
                    .predict()
                    .decode(binaryLogistic())
                    .interpret(p -> new TestInference(Result.PASS))
                    .orError(e -> new TestInference(Result.FAIL))
                    .decision();

            assertEquals(Result.FAIL, decision);
        }
    }

    // ── Escape hatches ────────────────────────────────────────────────────────

    @Nested
    class EscapeHatches {

        @Test
        void predicted_outcome_exposes_raw_success() throws Exception {
            Outcome<DefaultPrediction<float[][]>, PredictionError> outcome =
                    predictor(model, matrix(1f, 2f))
                            .translate(m -> m)
                            .predict()
                            .outcome();

            assertTrue(outcome.isSuccess());
        }

        @Test
        void predicted_outcome_exposes_raw_failure() {
            Outcome<DefaultPrediction<float[][]>, PredictionError> outcome =
                    predictor(model, (DMatrix) null)
                            .translate(m -> m)
                            .predict()
                            .outcome();

            assertTrue(outcome.isFailure());
        }

        @Test
        void decoded_outcome_exposes_raw_outcome_before_decoding() throws Exception {
            Outcome<DefaultPrediction<float[][]>, PredictionError> outcome =
                    predictor(model, matrix(1f, 2f))
                            .translate(m -> m)
                            .predict()
                            .decode(binaryLogistic())
                            .outcome();

            assertTrue(outcome.isSuccess());
        }

        @Test
        void decoded_returns_typed_predictions_on_success() throws Exception {
            Outcome<Predictions<Double, DefaultPrediction<Double>>, PredictionError> decoded =
                    predictor(model, matrix(1f, 2f))
                            .translate(m -> m)
                            .predict()
                            .decode(binaryLogistic())
                            .decoded();

            assertTrue(decoded.isSuccess());
            int size = decoded.fold(Predictions::size, e -> -1);
            assertEquals(1, size);
        }

        @Test
        void decoded_returns_failure_when_prediction_failed() {
            Outcome<Predictions<Double, DefaultPrediction<Double>>, PredictionError> decoded =
                    predictor(model, (DMatrix) null)
                            .translate(m -> m)
                            .predict()
                            .decode(binaryLogistic())
                            .decoded();

            assertTrue(decoded.isFailure());
        }
    }
}