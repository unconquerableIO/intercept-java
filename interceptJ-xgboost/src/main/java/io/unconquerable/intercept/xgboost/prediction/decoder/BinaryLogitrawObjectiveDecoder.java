package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.Arrays;


/**
 * A {@link PredictionsDecoder} for the XGBoost {@code binary:logitraw} objective.
 *
 * <p>XGBoost outputs raw log-odds for this objective rather than a probability.  The
 * injected {@link Normalizer} (typically a
 * {@link io.unconquerable.intercept.xgboost.normalizer.SigmoidNormalizer}) is applied to
 * each row's single score to recover a probability in {@code [0, 1]}.
 *
 * @param normalizer the normaliser used to transform each raw log-odds score to a probability
 * @author Rizwan Idrees
 */
public record BinaryLogitrawObjectiveDecoder(Normalizer<Float, Double> normalizer)
        implements PredictionsDecoder<Double, DefaultPrediction<Double>> {

    /**
     * Decodes and normalises predictions from a {@code binary:logitraw} raw output matrix.
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