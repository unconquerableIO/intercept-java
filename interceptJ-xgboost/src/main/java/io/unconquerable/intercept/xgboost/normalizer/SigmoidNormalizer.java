package io.unconquerable.intercept.xgboost.normalizer;

import jakarta.annotation.Nonnull;

/**
 * A {@link Normalizer} that applies the sigmoid (logistic) function to a raw score.
 *
 * <p>Maps any real-valued float to a probability in the open interval {@code (0, 1)} via
 * {@code σ(x) = 1 / (1 + e^(-x))}.
 * Typically used after a binary-classification XGBoost model whose objective does not
 * already apply a sigmoid transform (e.g. {@code reg:squarederror} used as a scorer).
 *
 * @author Rizwan Idrees
 */
public record SigmoidNormalizer() implements Normalizer<Float, Double> {

    /**
     * Applies the sigmoid function to {@code rawScore}.
     *
     * @param rawScore the raw model output; never {@code null}
     * @return a probability in {@code (0, 1)}
     */
    @Override
    public Double normalize(@Nonnull Float rawScore) {
        return 1.0 / (1.0 + Math.exp(-rawScore));
    }
}