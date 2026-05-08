package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * A {@link PredictionsDecoder} for the XGBoost {@code reg:logistic} objective.
 *
 * <p>XGBoost outputs raw log-odds via the logit link function for this objective.  The
 * injected {@link Normalizer} (typically a
 * {@link io.unconquerable.intercept.xgboost.normalizer.SigmoidNormalizer}) transforms each
 * log-odds value to a probability in {@code [0, 1]}.  The result is semantically equivalent
 * to {@code binary:logitraw} but is produced by a regression model trained with a logistic
 * loss.
 *
 * @param normalizer the normalizer used to transform each raw log-odds score to a probability
 * @author Rizwan Idrees
 */
public record RegressionLogisticObjectiveDecoder(Normalizer<Float, Double> normalizer)
        implements PredictionsDecoder<Double, DefaultPrediction<Double>> {

    /**
     * Decodes and normalizes predictions from a {@code reg:logistic} raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains exactly one log-odds value
     * @return a {@link Predictions} collection with one normalised {@code Double} probability per input row
     */
    @Override
    public Predictions<Double, DefaultPrediction<Double>> decode(@Nonnull float[][] rawResult) {
        return new Predictions<>(Arrays.stream(rawResult)
                .map(row -> new DefaultPrediction<>(normalizer.normalize(row[0])))
                .toList());
    }
}