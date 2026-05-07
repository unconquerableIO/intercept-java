package io.unconquerable.intercept.functional;

import java.util.function.Function;

/**
 * A disjoint-union type representing a value that is either a {@link Left} or a {@link Right}.
 *
 * <p>By convention in this library, {@code Left} carries a success value and {@code Right}
 * carries an error — for example:
 * <pre>{@code
 * Either<? extends Prediction<?>, Error> result = predictor.predict(matrix);
 *
 * result.fold(
 *     prediction -> handle(prediction),
 *     error      -> log(error.message())
 * );
 * }</pre>
 *
 * <p>Construct instances via the {@link #left(Object)} and {@link #right(Object)} factory
 * methods.  Transform or chain values with {@link #mapLeft}, {@link #mapRight},
 * {@link #flatMapLeft}, and {@link #flatMapRight}.  Collapse both sides into a single value
 * with {@link #fold}.
 *
 * @param <L> the type of the left (success) value
 * @param <R> the type of the right (error) value
 * @author Rizwan Idrees
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    /**
     * The left (success) variant of {@link Either}.
     *
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @param value the left value
     */
    record Left<L, R>(L value) implements Either<L, R> {}

    /**
     * The right (error) variant of {@link Either}.
     *
     * @param <L> the type of the left value
     * @param <R> the type of the right value
     * @param value the right value
     */
    record Right<L, R>(R value) implements Either<L, R> {}

    /**
     * Constructs a {@link Left} instance wrapping the given value.
     *
     * @param <L>   the left type
     * @param <R>   the right type
     * @param value the success value
     * @return a {@code Left} containing {@code value}
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Constructs a {@link Right} instance wrapping the given value.
     *
     * @param <L>   the left type
     * @param <R>   the right type
     * @param value the error value
     * @return a {@code Right} containing {@code value}
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Returns {@code true} if this is a {@link Left}.
     *
     * @return {@code true} for {@link Left}, {@code false} for {@link Right}
     */
    default boolean isLeft() {
        return this instanceof Left<L, R>;
    }

    /**
     * Returns {@code true} if this is a {@link Right}.
     *
     * @return {@code true} for {@link Right}, {@code false} for {@link Left}
     */
    default boolean isRight() {
        return this instanceof Right<L, R>;
    }

    /**
     * Collapses both sides into a single value by applying the appropriate mapper.
     *
     * @param <T>     the result type
     * @param onLeft  function applied when this is a {@link Left}
     * @param onRight function applied when this is a {@link Right}
     * @return the result of whichever mapper was applied
     */
    default <T> T fold(Function<? super L, ? extends T> onLeft, Function<? super R, ? extends T> onRight) {
        return switch (this) {
            case Left<L, R> l -> onLeft.apply(l.value());
            case Right<L, R> r -> onRight.apply(r.value());
        };
    }

    /**
     * Transforms the left value, leaving a {@link Right} unchanged.
     *
     * @param <T>    the new left type
     * @param mapper function applied to the left value
     * @return a new {@link Left} containing the mapped value, or the original {@link Right}
     */
    default <T> Either<T, R> mapLeft(Function<? super L, ? extends T> mapper) {
        return switch (this) {
            case Left<L, R> l -> Either.left(mapper.apply(l.value()));
            case Right<L, R> r -> Either.right(r.value());
        };
    }

    /**
     * Transforms the right value, leaving a {@link Left} unchanged.
     *
     * @param <T>    the new right type
     * @param mapper function applied to the right value
     * @return the original {@link Left}, or a new {@link Right} containing the mapped value
     */
    default <T> Either<L, T> mapRight(Function<? super R, ? extends T> mapper) {
        return switch (this) {
            case Left<L, R> l -> Either.left(l.value());
            case Right<L, R> r -> Either.right(mapper.apply(r.value()));
        };
    }

    /**
     * Applies a mapper to the left value that itself returns an {@link Either}, leaving a
     * {@link Right} unchanged.
     *
     * @param <T>    the new left type
     * @param mapper function applied to the left value; returns a new {@link Either}
     * @return the result of {@code mapper} when this is a {@link Left}, otherwise the original {@link Right}
     */
    default <T> Either<T, R> flatMapLeft(Function<? super L, ? extends Either<T, R>> mapper) {
        return switch (this) {
            case Left<L, R> l -> mapper.apply(l.value());
            case Right<L, R> r -> Either.right(r.value());
        };
    }

    /**
     * Applies a mapper to the right value that itself returns an {@link Either}, leaving a
     * {@link Left} unchanged.
     *
     * @param <T>    the new right type
     * @param mapper function applied to the right value; returns a new {@link Either}
     * @return the original {@link Left}, or the result of {@code mapper} when this is a {@link Right}
     */
    default <T> Either<L, T> flatMapRight(Function<? super R, ? extends Either<L, T>> mapper) {
        return switch (this) {
            case Left<L, R> l -> Either.left(l.value());
            case Right<L, R> r -> mapper.apply(r.value());
        };
    }
}