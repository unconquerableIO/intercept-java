package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.xgboost.model.ModelLoader;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.PredictionError;
import io.unconquerable.intercept.xgboost.prediction.Outcome;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;

/**
 * Runs inference on a loaded XGBoost model and returns an {@link Outcome}.
 *
 * <p>The model is resolved and deserialized eagerly at construction time via the supplied
 * {@link ModelLoader}.  Once constructed, {@link #predict(DMatrix)} can be called repeatedly
 * for each inference request.  Any XGBoost or runtime failure is captured as a
 * {@link Outcome.Failure} rather than propagating as an exception.
 *
 * @author Rizwan Idrees
 */
public class XGBoostPredictor {

    private final Booster booster;

    /**
     * Creates a new {@code XGBoostPredictor} by loading the model from the given source.
     *
     * @param loader the {@link ModelLoader} used to resolve and deserialize the model;
     *               never {@code null}
     * @throws io.unconquerable.intercept.xgboost.model.ModelLoaderException if the model
     *         cannot be loaded
     */
    public XGBoostPredictor(ModelLoader loader) {
        this.booster = loader.load();
    }

    /**
     * Runs inference on the given feature matrix.
     *
     * <p>Returns a {@link Outcome.Success} containing the raw prediction on success,
     * or a {@link Outcome.Failure} containing a {@link PredictionError} if inference
     * fails.
     *
     * @param matrix the feature matrix for the input batch; never {@code null}
     * @return a {@link Outcome} with the raw prediction on success or an error on failure
     */
    public Outcome<DefaultPrediction<float[][]>, PredictionError> predict(DMatrix matrix) {
        try {
            return Outcome.success(new DefaultPrediction<>(booster.predict(matrix)));
        } catch (Exception e) {
            return Outcome.failure(PredictionError.of(e));
        }
    }

}
