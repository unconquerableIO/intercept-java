package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

@FunctionalInterface
public interface Normalizer<I, O> {
    O normalize(@Nonnull I input);
}
