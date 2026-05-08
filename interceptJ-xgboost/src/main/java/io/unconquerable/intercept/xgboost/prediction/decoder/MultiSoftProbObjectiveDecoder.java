package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * A {@link PredictionsDecoder} for the XGBoost {@code multi:softprob} objective.
 *
 * <p>XGBoost applies the softmax function internally for this objective, so each row
 * contains one probability per class, summing to {@code 1.0}.  This decoder reads the
 * probability at {@link #targetClassIndex} from each row, yielding the model's confidence
 * that each sample belongs to the target class.
 *
 * @param targetClassIndex zero-based index of the class whose probability is decoded
 * @author Rizwan Idrees
 */
public record MultiSoftProbObjectiveDecoder(
        int targetClassIndex) implements PredictionsDecoder<Double, DefaultPrediction<Double>> {

    /**
     * Decodes the target-class probability from a {@code multi:softprob} raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains one probability per class, summing to {@code 1.0}
     * @return a {@link Predictions} collection with the {@link #targetClassIndex} probability per input row
     */
    @Override
    public Predictions<Double, DefaultPrediction<Double>> decode(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>((double) row[targetClassIndex]))
                .toList());
    }
}