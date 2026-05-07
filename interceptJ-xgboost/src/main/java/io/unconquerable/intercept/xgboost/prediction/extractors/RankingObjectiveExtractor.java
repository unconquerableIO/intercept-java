package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.stream.IntStream;

/**
 * A {@link PredictionsExtractor} for XGBoost ranking objectives (e.g. {@code rank:pairwise},
 * {@code rank:ndcg}, {@code rank:map}).
 *
 * <p>XGBoost outputs relevance scores that are only meaningful relative to each other within
 * the same inference batch.  This extractor flattens the per-row scores into a single array,
 * delegates to the injected {@link Normalizer} (typically a
 * {@link io.unconquerable.intercept.xgboost.normalizer.RankingNormalizer}) to rescale the
 * entire batch to {@code [0, 1]}, then wraps each normalised score in a {@code DefaultPrediction}.
 *
 * @param normalizer the normaliser applied across the full batch of raw ranking scores
 * @author Rizwan Idrees
 */
public record RankingObjectiveExtractor(
        Normalizer<float[], double[]> normalizer) implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    /**
     * Extracts and batch-normalises predictions from a ranking objective raw output matrix.
     *
     * @param rawResult the raw score matrix; each row contains exactly one ranking score
     * @return a {@link Predictions} collection with each score rescaled to {@code [0, 1]}
     *         relative to the batch min/max
     */
    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        float[] flat = new float[rawResult.length];
        for (int i = 0; i < rawResult.length; i++){
            flat[i] = rawResult[i][0];
        }
        double[] normalised = normalizer.normalize(flat);
        return new Predictions<>(IntStream.range(0, rawResult.length)
                .mapToObj(i -> new DefaultPrediction<>(normalised[i]))
                .toList());
    }
}