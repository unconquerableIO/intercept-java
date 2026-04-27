package io.unconquerable.intercept.detect;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A {@link Detector} decorator that guards execution behind a composable boolean condition.
 *
 * <p>In real-world fraud pipelines some detectors are expensive (remote calls, ML inference)
 * or only relevant under specific circumstances (e.g., only check 3-D Secure for card payments).
 * {@code ConditionalDetector} lets you attach a runtime predicate to any {@link Detector} so
 * that the underlying analysis runs only when truly needed.
 *
 * <p>When the condition evaluates to {@code false}, the detector is bypassed. The result is
 * determined as follows:
 * <ol>
 *   <li>If a custom {@code whenSkipped} supplier was provided via
 *       {@link ConditionalDetectorBuilder#whenSkipped(Supplier)}, its value is returned.</li>
 *   <li>Otherwise a {@link DetectedStatus} with {@link DetectedStatus.Status#SKIPPED} is
 *       returned as the default, preserving the detector's slot in the result list without
 *       producing a false negative.</li>
 * </ol>
 *
 * <p>Instances are created through the fluent builder exposed by
 * {@link Detectors#detector(Detector)} (preferred) or {@link #detector(Detector)}:
 *
 * <pre>{@code
 * // Basic conditional — skip when feature flag is off
 * Detector<String> conditional = Detectors.detector(velocityDetector)
 *     .when(() -> request.isAuthenticated())
 *     .and(() -> featureFlags.isVelocityCheckEnabled())
 *     .build();
 *
 * // Custom skip result — return NOT_DETECTED instead of SKIPPED
 * Detector<String> withCustomSkip = Detectors.detector(velocityDetector)
 *     .when(() -> request.isAuthenticated())
 *     .whenSkipped(() -> new DetectedStatus<>("velocity", null, DetectedStatus.Status.NOT_DETECTED))
 *     .build();
 *
 * interceptor()
 *     .detect(request.getUserId(), conditional)
 *     .decide(decider);
 * }</pre>
 *
 * @param <T>         the type of the target value the wrapped detector analyses
 * @param detector    the underlying detector to delegate to when the condition is met;
 *                    must not be {@code null}
 * @param condition   the runtime guard evaluated on every {@link #detect(Object)} call;
 *                    {@code true} means the wrapped detector runs; must not be {@code null}
 * @param whenSkipped optional factory for the result returned when the condition is
 *                    {@code false}; when {@code null} the default
 *                    {@link DetectedStatus.Status#SKIPPED} result is used
 * @author Rizwan Idrees
 * @see Detectors
 * @see DetectedStatus.Status#SKIPPED
 */
public record ConditionalDetector<T>(Detector<T> detector,
                                     BooleanSupplier condition,
                                     Supplier<Detected<T>> whenSkipped) implements Detector<T> {

    /**
     * Creates a new {@link ConditionalDetectorBuilder} for the given detector.
     *
     * <p>Prefer the equivalent factory method on {@link Detectors#detector(Detector)} for
     * consistency with the rest of the fluent API.
     *
     * @param <T>      the type of the target value the detector analyses
     * @param detector the detector to wrap; must not be {@code null}
     * @return a builder for composing runtime conditions
     */
    public static <T> ConditionalDetectorBuilder<T> detector(Detector<T> detector) {
        return new ConditionalDetectorBuilder<>(detector);
    }

    /**
     * Fluent builder for constructing a {@link ConditionalDetector} with one or more composed
     * boolean conditions.
     *
     * <p>Conditions are evaluated lazily at detection time. Multiple conditions may be chained
     * with {@link #and(BooleanSupplier)} and {@link #or(BooleanSupplier)}; they are composed in
     * the order the builder methods are called.
     *
     * @param <T> the type of the target value the underlying detector analyses
     */
    public static class ConditionalDetectorBuilder<T> {

        private final Detector<T> detector;
        private BooleanSupplier composed = () -> true;
        private Supplier<Detected<T>> whenSkipped;

        ConditionalDetectorBuilder(Detector<T> detector) {
            this.detector = Objects.requireNonNull(detector);
        }

        /**
         * Sets the primary condition that must be {@code true} for the detector to run.
         *
         * <p>Replaces any previously set condition. To compose multiple conditions, call
         * {@link #and(BooleanSupplier)} or {@link #or(BooleanSupplier)} after this method.
         *
         * @param condition the runtime guard; must not be {@code null}
         * @return this builder for fluent chaining
         */
        public ConditionalDetectorBuilder<T> when(BooleanSupplier condition) {
            this.composed = condition;
            return this;
        }

        /**
         * Appends an additional condition using logical AND.
         *
         * <p>The detector runs only if both the previously composed condition <em>and</em>
         * {@code next} evaluate to {@code true}. Short-circuit evaluation applies: {@code next}
         * is not evaluated if the prior condition is already {@code false}.
         *
         * @param next the additional condition; must not be {@code null}
         * @return this builder for fluent chaining
         */
        public ConditionalDetectorBuilder<T> and(BooleanSupplier next) {
            BooleanSupplier prev = this.composed;
            this.composed = () -> prev.getAsBoolean() && next.getAsBoolean();
            return this;
        }

        /**
         * Appends an additional condition using logical OR.
         *
         * <p>The detector runs if either the previously composed condition <em>or</em>
         * {@code next} evaluates to {@code true}. Short-circuit evaluation applies: {@code next}
         * is not evaluated if the prior condition is already {@code true}.
         *
         * @param next the alternative condition; must not be {@code null}
         * @return this builder for fluent chaining
         */
        public ConditionalDetectorBuilder<T> or(BooleanSupplier next) {
            BooleanSupplier prev = this.composed;
            this.composed = () -> prev.getAsBoolean() || next.getAsBoolean();
            return this;
        }

        /**
         * Overrides the result returned when the condition evaluates to {@code false}.
         *
         * <p>By default, a skipped detector returns a {@link DetectedStatus} with
         * {@link DetectedStatus.Status#SKIPPED}. Use this method when a different result is
         * required — for example, returning {@link DetectedStatus.Status#NOT_DETECTED} to treat
         * a skipped detector as a clean signal rather than an absent one.
         *
         * <p>If not called, the built {@link ConditionalDetector} falls back to the default
         * {@code SKIPPED} result automatically.
         *
         * @param whenSkipped the factory that produces the result when this detector is skipped;
         *                    must not be {@code null}
         * @return this builder for fluent chaining
         */
        public ConditionalDetectorBuilder<T> whenSkipped(Supplier<Detected<T>> whenSkipped) {
            this.whenSkipped = whenSkipped;
            return this;
        }

        /**
         * Builds and returns the configured {@link ConditionalDetector}.
         *
         * <p>The {@code whenSkipped} supplier is optional. If
         * {@link #whenSkipped(Supplier)} was not called, the built detector will fall back to
         * returning a {@link DetectedStatus} with {@link DetectedStatus.Status#SKIPPED} whenever
         * the condition is {@code false}.
         *
         * @return a new {@code ConditionalDetector} with the composed condition and optional
         *         custom skip result
         */
        public ConditionalDetector<T> build() {
            return new ConditionalDetector<>(detector, composed, whenSkipped);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the wrapped detector's {@link Detector#name()} so that results produced by
     * this conditional detector are attributed to the underlying detector.
     */
    @Override
    public String name() {
        return detector.name();
    }

    /**
     * Evaluates the condition and either delegates to the wrapped detector or returns a skip result.
     *
     * <p>Resolution order when the condition is {@code false}:
     * <ol>
     *   <li>If a custom {@code whenSkipped} supplier was provided, its result is returned.</li>
     *   <li>Otherwise a {@link DetectedStatus} carrying {@link DetectedStatus.Status#SKIPPED}
     *       and the original {@code target} value is returned as the default.</li>
     * </ol>
     *
     * @param target the value to analyse if the condition is {@code true}; passed through to
     *               the default {@code SKIPPED} result when the condition is {@code false} so
     *               that the target is always traceable in the detection output
     * @return the wrapped detector's {@link Detected} result when the condition is {@code true};
     *         otherwise the custom skip result if one was configured, or a default
     *         {@link DetectedStatus} with {@link DetectedStatus.Status#SKIPPED}
     */
    @Override
    public Detected<T> detect(T target) {
        if (condition.getAsBoolean()) {
            return detector.detect(target);
        }

        return Optional.ofNullable(whenSkipped)
                .map(Supplier::get)
                .orElseGet(() -> new DetectedStatus<>(detector.name(), target, DetectedStatus.Status.SKIPPED));
    }

}
