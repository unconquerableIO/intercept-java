package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * Objective multi:softprob
 * XGBoost applies softmax internally — each row contains one probability
 * per class, summing to 1.0. Extracts the probability for the target class.
 *
 * @param targetClassIndex index of the class to extract probability for
 */
public record MultiSoftProbObjectiveExtractor(
        int targetClassIndex) implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>((double) row[targetClassIndex]))
                .toList());
    }
}