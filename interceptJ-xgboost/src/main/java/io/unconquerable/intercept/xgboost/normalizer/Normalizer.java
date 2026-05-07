package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

/**
 * Functional interface for normalizing raw XGBoost output into a more interpretable form.
 *
 * <p>Implementations transform a raw prediction value of type {@code I} (e.g. a raw float
 * score) into a normalized value of type {@code O} (e.g. a probability in {@code [0, 1]}).
 * Common implementations include {@link SigmoidNormalizer}, {@link MinMaxNormalizer}, and
 * {@link RankingNormalizer}.
 *
 * @param <I> the type of the raw input value
 * @param <O> the type of the normalized output value
 * @author Rizwan Idrees
 */
@FunctionalInterface
public interface Normalizer<I, O> {

    /**
     * Normalises the given raw input value.
     *
     * @param input the raw value to normalize; never {@code null}
     * @return the normalized value; never {@code null}
     */
    O normalize(@Nonnull I input);
}
