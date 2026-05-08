package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * A {@link PredictionsDecoder} for the XGBoost {@code reg:squarederror} objective.
 *
 * <p>XGBoost outputs a raw continuous value in the same unit as the training labels (MSE
 * minimisation).  The injected {@link Normalizer} is applied to each score — use a
 * {@link io.unconquerable.intercept.xgboost.normalizer.MinMaxNormalizer} to rescale to
 * {@code [0, 1]}, or supply a pass-through implementation when the raw value is already
 * meaningful.
 *
 * @param normalizer the normaliser applied to each raw regression output
 * @author Rizwan Idrees
 */
public record RegressionSquaredErrorObjectiveDecoder(Normalizer<Float, Double> normalizer)
        implements PredictionsDecoder<Double, DefaultPrediction<Double>> {

    /**
     * Decodes and normalises predictions from a {@code reg:squarederror} raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains exactly one continuous regression value
     * @return a {@link Predictions} collection with one normalised {@code Double} value per input row
     */
    @Override
    public Predictions<Double, DefaultPrediction<Double>> decode(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>(normalizer.normalize(row[0])))
                .toList());
    }
}