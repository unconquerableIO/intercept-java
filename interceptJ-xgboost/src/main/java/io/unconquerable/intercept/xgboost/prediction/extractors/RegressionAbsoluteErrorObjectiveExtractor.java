package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * Objective reg:absoluteerror
 * XGBoost outputs a raw continuous value in the same unit as the training labels.
 * The injected Normalizer scales the value if needed (e.g. min-max),
 * or passthrough if the raw value is meaningful as-is.
 */
public record RegressionAbsoluteErrorObjectiveExtractor(
        Normalizer<Float, Double> normalizer) implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>(normalizer.normalize(row[0])))
                .toList());
    }
}