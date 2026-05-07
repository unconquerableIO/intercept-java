package io.unconquerable.intercept.xgboost.prediction;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * The sole {@link Error} implementation, carrying a message and an optional root cause.
 *
 * <p>Returned as the right side of an
 * {@link io.unconquerable.intercept.functional.Either Either&lt;? extends Prediction, Error&gt;}
 * when {@link io.unconquerable.intercept.xgboost.predictor.XGBoostPredictor#predict} fails.
 * Use the factory methods to construct instances:
 * <ul>
 *   <li>{@link #of(String)} — when no underlying exception is available</li>
 *   <li>{@link #of(Throwable)} — to wrap a caught exception</li>
 * </ul>
 *
 * @param message a human-readable description of the failure; never {@code null}
 * @param cause   the underlying exception, or {@code null} if not applicable
 * @author Rizwan Idrees
 */
public record PredictionError(@Nonnull String message, @Nullable Throwable cause) implements Error {

    /**
     * Creates a {@code PredictionError} with the given message and no cause.
     *
     * @param message a human-readable description of the failure; never {@code null}
     * @return a new {@code PredictionError}
     */
    public static PredictionError of(String message) {
        return new PredictionError(message, null);
    }

    /**
     * Creates a {@code PredictionError} by extracting the message from the given throwable.
     *
     * @param cause the underlying exception; never {@code null}
     * @return a new {@code PredictionError} wrapping {@code cause}
     */
    public static PredictionError of(Throwable cause) {
        return new PredictionError(cause.getMessage(), cause);
    }
}