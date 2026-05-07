package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

public record RankingNormalizer() implements Normalizer<float[], double[]> {

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