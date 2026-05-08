package io.unconquerable.intercept.xgboost.prediction;

import java.util.function.Function;

/**
 * The outcome of running an XGBoost prediction — either a successful {@link Success} carrying
 * a value of type {@code O}, or a {@link Failure} carrying an error of type {@code E}.
 *
 * <p>Construct instances via the factory methods:
 * <pre>{@code
 * Outcome<DefaultPrediction<float[][]>, PredictionError> outcome = predictor.predict(matrix);
 *
 * outcome.fold(
 *     prediction -> decoder.decode(prediction.value()),
 *     error      -> log(error.message())
 * );
 * }</pre>
 *
 * @param <O> the type of the value carried on success
 * @param <E> the type of the error carried on failure
 * @author Rizwan Idrees
 */
public sealed interface Outcome<O, E>
        permits Outcome.Success, Outcome.Failure {

    /**
     * The success variant — carries the value produced by the model.
     *
     * @param <O>     the type of the value
     * @param <E>     the type of the error (unused on success)
     * @param outcome the value; never {@code null}
     */
    record Success<O, E>(O outcome) implements Outcome<O, E> {}

    /**
     * The failure variant — carries the error that prevented a result.
     *
     * @param <O>   the type of the value (unused on failure)
     * @param <E>   the type of the error
     * @param error the error describing what went wrong; never {@code null}
     */
    record Failure<O, E>(E error) implements Outcome<O, E> {}

    /**
     * Creates a {@link Success} wrapping the given value.
     *
     * @param <O>     the type of the value
     * @param <E>     the type of the error
     * @param outcome the value; never {@code null}
     * @return a {@link Success} containing {@code outcome}
     */
    static <O, E> Outcome<O, E> success(O outcome) {
        return new Success<>(outcome);
    }

    /**
     * Creates a {@link Failure} wrapping the given error.
     *
     * @param <O>   the type of the value
     * @param <E>   the type of the error
     * @param error the error to wrap; never {@code null}
     * @return a {@link Failure} containing {@code error}
     */
    static <O, E> Outcome<O, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Returns {@code true} if this is a {@link Success}.
     *
     * @return {@code true} for {@link Success}, {@code false} for {@link Failure}
     */
    default boolean isSuccess() {
        return this instanceof Success<O, E>;
    }

    /**
     * Returns {@code true} if this is a {@link Failure}.
     *
     * @return {@code true} for {@link Failure}, {@code false} for {@link Success}
     */
    default boolean isFailure() {
        return this instanceof Failure<O, E>;
    }

    /**
     * Collapses both outcomes into a single value by applying the matching function.
     *
     * @param <T>       the result type
     * @param onSuccess applied when this is a {@link Success}
     * @param onFailure applied when this is a {@link Failure}
     * @return the result of whichever function was applied
     */
    default <T> T fold(Function<? super O, ? extends T> onSuccess,
                       Function<? super E, ? extends T> onFailure) {
        return switch (this) {
            case Success<O, E> s -> onSuccess.apply(s.outcome());
            case Failure<O, E> f -> onFailure.apply(f.error());
        };
    }

    /**
     * Transforms the value on success, preserving the error type, leaving a {@link Failure} unchanged.
     *
     * @param <Q>    the new value type
     * @param mapper function applied to the value
     * @return a new {@link Success} containing the mapped value, or the original {@link Failure}
     */
    default <Q> Outcome<Q, E> map(Function<? super O, ? extends Q> mapper) {
        return switch (this) {
            case Success<O, E> s -> Outcome.success(mapper.apply(s.outcome()));
            case Failure<O, E> f -> Outcome.failure(f.error());
        };
    }

    /**
     * Chains a step that itself returns an {@link Outcome}, preserving the error type,
     * leaving a {@link Failure} unchanged.
     *
     * @param <Q>    the new value type
     * @param mapper function applied to the value; returns a new {@link Outcome}
     * @return the result of {@code mapper} on success, or the original {@link Failure}
     */
    default <Q> Outcome<Q, E> flatMap(Function<? super O, ? extends Outcome<Q, E>> mapper) {
        return switch (this) {
            case Success<O, E> s -> mapper.apply(s.outcome());
            case Failure<O, E> f -> Outcome.failure(f.error());
        };
    }
}