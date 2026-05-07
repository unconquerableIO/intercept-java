package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * multi:softmax objective
 *
 * @param targetClassIndex
 */
public record MultiSoftMaxObjectiveExtractor(int targetClassIndex)
        implements PredictionsExtractor<Integer, DefaultPrediction<Integer>> {

    @Override
    public Predictions<Integer, DefaultPrediction<Integer>> extract(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row ->
                        new DefaultPrediction<>((int) row[0] == targetClassIndex ? 1 : 0))
                .toList());
    }
}
