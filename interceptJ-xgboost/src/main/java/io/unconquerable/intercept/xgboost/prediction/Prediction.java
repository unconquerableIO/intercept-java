package io.unconquerable.intercept.xgboost.prediction;

/**
 * Represents a single typed prediction produced by an XGBoost model.
 *
 * <p>The type parameter {@code P} captures the normalised form of the prediction value —
 * e.g. {@code Double} for probability or regression output, {@code Integer} for a class
 * label.  Implementations include {@link DefaultPrediction} for general-purpose use.
 *
 * @param <P> the type of the prediction value
 * @author Rizwan Idrees
 */
public interface Prediction<P> {

    /**
     * Returns the prediction value.
     *
     * @return the normalised prediction value; never {@code null}
     */
    P value();

}
