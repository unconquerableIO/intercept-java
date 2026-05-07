package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * Objective reg:logistic
 * XGBoost outputs raw log-odds via the logit link function.
 * The injected Normalizer (sigmoid) transforms the log-odds to [0,1].
 * Result is a probability — semantically equivalent to binary:logitraw
 * but produced by a regression model.
 */
public record RegressionLogisticObjectiveExtractor(Normalizer<Float, Double> normalizer)
        implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>(normalizer.normalize(row[0])))
                .toList());
    }
}