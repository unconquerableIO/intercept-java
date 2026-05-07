package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

/**
 * Objective binary:logistic
 * XGBoost applies the logistic function internally — rawResult is already
 * a probability in [0,1], no normalisation needed.
 */
public record BinaryLogisticObjectiveExtractor() implements PredictionsExtractor<Double, DefaultPrediction<Double>> {


    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        final List<DefaultPrediction<Double>> list = Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>((double) row[0]))
                .toList();
        return new Predictions<>(list);
    }
}
