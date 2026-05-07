package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

public record SigmoidNormalizer() implements Normalizer<Float, Double> {

    @Override
    public Double normalize(@Nonnull Float rawScore) {
        return 1.0 / (1.0 + Math.exp(-rawScore));
    }
}