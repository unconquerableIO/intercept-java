package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link PredictionsExtractor} for the XGBoost {@code binary:logistic} objective.
 *
 * <p>XGBoost applies the logistic (sigmoid) function internally for this objective, so each
 * row in the raw result already contains a single probability value in {@code [0, 1]}.
 * No additional normalization is required; the raw {@code float} is widened to {@code Double}.
 *
 * @author Rizwan Idrees
 */
public record BinaryLogisticObjectiveExtractor() implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    /**
     * Extracts predictions from a {@code binary:logistic} raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains exactly one probability in {@code [0, 1]}
     * @return a {@link Predictions} collection with one {@code Double} probability per input row
     */
    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        final List<DefaultPrediction<Double>> list = Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>((double) row[0]))
                .toList();
        return new Predictions<>(list);
    }
}
