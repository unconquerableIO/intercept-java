package io.unconquerable.intercept.xgboost.prediction;

/**
 * Interface representing a prediction failure in the XGBoost pipeline.
 *
 * <p>Returned inside a {@link io.unconquerable.intercept.xgboost.prediction.Outcome.Failure}
 * when {@link io.unconquerable.intercept.xgboost.predictor.XGBoostPredictor#predict} cannot
 * produce a result.
 *
 * @author Rizwan Idrees
 */
public interface Error {

    /**
     * Returns a human-readable description of the failure.
     *
     * @return the error message; never {@code null}
     */
    String message();
}