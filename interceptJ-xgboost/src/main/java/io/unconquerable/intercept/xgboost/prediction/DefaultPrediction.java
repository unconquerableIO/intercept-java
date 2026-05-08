package io.unconquerable.intercept.xgboost.prediction;

/**
 * A general-purpose {@link Prediction} implementation that wraps a single typed value.
 *
 * <p>Used by all built-in {@link io.unconquerable.intercept.xgboost.prediction.decoder.PredictionsDecoder}
 * implementations to carry normalized prediction values (e.g. probabilities, regression
 * outputs, or class-match indicators) out of the extraction pipeline.
 *
 * @param <T>   the type of the prediction value
 * @param value the normalized prediction value
 * @author Rizwan Idrees
 */
public record DefaultPrediction<T>(T value) implements Prediction<T> {
}
