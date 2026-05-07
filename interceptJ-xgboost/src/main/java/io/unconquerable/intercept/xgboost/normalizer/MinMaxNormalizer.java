package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

public record MinMaxNormalizer(double min, double max) implements Normalizer<Float, Double> {
    @Override
    public Double normalize(@Nonnull Float input) {
        if (min == max) return 0.0;
        return Math.clamp((input - min) / (max - min), 0.0, 1.0);
    }
}
