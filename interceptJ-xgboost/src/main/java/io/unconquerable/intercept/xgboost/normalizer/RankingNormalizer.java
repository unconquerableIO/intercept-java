package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

/**
 * A {@link Normalizer} that rescales an array of raw ranking scores to {@code [0, 1]}.
 *
 * <p>Derives the min and max from the input array itself and delegates each element to a
 * {@link MinMaxNormalizer}, preserving relative order while mapping all scores into a
 * comparable {@code [0, 1]} range.  Useful when comparing relevance scores produced by an
 * XGBoost ranker ({@code rank:ndcg}, {@code rank:pairwise}) across items in the same
 * prediction batch.
 *
 * @author Rizwan Idrees
 */
public record RankingNormalizer() implements Normalizer<float[], double[]> {

    /**
     * Normalises each element of {@code input} to {@code [0, 1]} relative to the array's
     * own min and max.
     *
     * @param input the raw ranking scores produced by the model; never {@code null}
     * @return a new array of the same length with each score mapped to {@code [0, 1]}
     */
    @Override
    public double[] normalize(@Nonnull float[] input) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (float v : input) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        MinMaxNormalizer minMaxNormalizer = new MinMaxNormalizer(min, max);
        double[] result = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = minMaxNormalizer.normalize(input[i]);
        }
        return result;
    }
}