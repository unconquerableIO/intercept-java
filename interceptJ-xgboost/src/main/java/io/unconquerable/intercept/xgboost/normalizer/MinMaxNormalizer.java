package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

/**
 * A {@link Normalizer} that rescales a raw score to {@code [0, 1]} using min-max normalization.
 *
 * <p>Given a known {@link #min} and {@link #max} for the score range, each value is mapped via:
 * <pre>
 *   normalised = clamp((x − min) / (max − min), 0.0, 1.0)
 * </pre>
 * When {@code min == max} (degenerate range), the result is always {@code 0.0}.
 *
 * @param min the lower bound of the expected score range
 * @param max the upper bound of the expected score range
 * @author Rizwan Idrees
 */
public record MinMaxNormalizer(double min, double max) implements Normalizer<Float, Double> {

    /**
     * Rescales {@code input} to {@code [0, 1]} using the configured {@code min}/{@code max} range.
     *
     * @param input the raw score to normalize; never {@code null}
     * @return a value in {@code [0, 1]}, or {@code 0.0} when {@code min == max}
     */
    @Override
    public Double normalize(@Nonnull Float input) {
        if (min == max) return 0.0;
        return Math.clamp((input - min) / (max - min), 0.0, 1.0);
    }
}
