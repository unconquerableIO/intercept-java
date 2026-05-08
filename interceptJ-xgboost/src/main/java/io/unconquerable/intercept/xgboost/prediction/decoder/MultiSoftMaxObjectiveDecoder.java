package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * A {@link PredictionsDecoder} for the XGBoost {@code multi:softmax} objective.
 *
 * <p>XGBoost outputs a single integer class label per sample for this objective.  Each
 * row's label is compared against {@link #targetClassIndex}: a match yields {@code 1},
 * a non-match yields {@code 0}, producing a binary indicator suitable for downstream
 * threshold-based decision logic.
 *
 * @param targetClassIndex the class label to treat as a positive match
 * @author Rizwan Idrees
 */
public record MultiSoftMaxObjectiveDecoder(int targetClassIndex)
        implements PredictionsDecoder<Integer, DefaultPrediction<Integer>> {

    /**
     * Decodes predictions from a {@code multi:softmax} raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains exactly one integer class label
     * @return a {@link Predictions} collection with {@code 1} for rows matching
     *         {@link #targetClassIndex} and {@code 0} for all others
     */
    @Override
    public Predictions<Integer, DefaultPrediction<Integer>> decode(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row ->
                        new DefaultPrediction<>((int) row[0] == targetClassIndex ? 1 : 0))
                .toList());
    }
}