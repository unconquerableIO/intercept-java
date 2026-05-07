package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.functional.Either;
import io.unconquerable.intercept.xgboost.model.ModelLoader;
import io.unconquerable.intercept.xgboost.prediction.*;
import io.unconquerable.intercept.xgboost.prediction.Error;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;

/**
 * Runs inference on a loaded XGBoost model and returns the result as an
 * {@link Either Either&lt;? extends Prediction, Error&gt;}.
 *
 * <p>The model is resolved and deserialized eagerly at construction time via the supplied
 * {@link ModelLoader}.  Once constructed, {@link #predict(DMatrix)} can be called repeatedly
 * for each inference request.  Any XGBoost or runtime failure is captured as a
 * {@link io.unconquerable.intercept.xgboost.prediction.PredictionError} on the right side of
 * the {@link Either} rather than propagating as an exception.
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
     * <p>Returns a {@link io.unconquerable.intercept.functional.Either.Left Left} containing
     * the raw prediction on success, or a
     * {@link io.unconquerable.intercept.functional.Either.Right Right} containing a
     * {@link io.unconquerable.intercept.xgboost.prediction.PredictionError} if inference fails.
     *
     * @param matrix the feature matrix for the input batch; never {@code null}
     * @return an {@link Either} with the raw prediction on the left or an error on the right
     */
    public Either<? extends Prediction<float[][]>, Error> predict(DMatrix matrix) {
        try {
            return Either.left(new DefaultPrediction<>(booster.predict(matrix)));
        } catch (Exception e) {
            return Either.right(PredictionError.of(e));
        }
    }

}
