package io.unconquerable.intercept.detect;

/**
 * Generic interface representing the outcome of a single {@link Detector} analysis.
 *
 * <p>Every {@link Detector}{@code <T>} produces a {@code Detected<T>} result that carries both
 * the original target value that was analyzed and the name of the detector that produced it.
 * The full collection of results from all registered detectors is gathered by
 * {@link io.unconquerable.intercept.Interceptor} and forwarded to a
 * {@link io.unconquerable.intercept.decide.Decider}, which inspects them to reach a
 * {@link io.unconquerable.intercept.decide.Decided} verdict.
 *
 * <p>Two concrete implementations are provided out of the box:
 * <ul>
 *   <li>{@link DetectedScore} — carries a numeric risk score suitable for threshold-based
 *       decisions (e.g. ML model output, composite risk index)</li>
 *   <li>{@link DetectedStatus} — carries a discrete signal ({@code DETECTED},
 *       {@code NOT_DETECTED}, or {@code SKIPPED}) for boolean-style detectors</li>
 * </ul>
 *
 * <p>The type parameter {@code <T>} ensures that the target value is preserved with its original
 * type throughout the pipeline, enabling {@link io.unconquerable.intercept.decide.Decider}
 * implementations to access the inspected value without casting.
 *
 * <p>Custom implementations may be created to carry richer, domain-specific detection metadata.
 *
 * @param <T> the type of the target value that was analysed by the {@link Detector}
 * @author Rizwan Idrees
 * @see DetectedScore
 * @see DetectedStatus
 * @see Detector
 */
public interface Detected<T> {

    /**
     * Returns the target value that was submitted to the {@link Detector} for analysis.
     *
     * <p>Carrying the target through the result allows
     * {@link io.unconquerable.intercept.decide.Decider} implementations and audit consumers
     * (e.g. {@link io.unconquerable.intercept.send.Sender}) to inspect the exact value that
     * produced a given signal without requiring a separate lookup.
     *
     * <p>May be {@code null} for synthetic results — such as those produced by a
     * {@link ConditionalDetector} when its condition evaluates to {@code false} and a custom
     * {@code whenSkipped} supplier is not configured.
     *
     * @return the analysed target value; may be {@code null} for skipped detections
     */
    T target();

    /**
     * Returns the name of the {@link Detector} that produced this result.
     *
     * <p>This value corresponds to {@link Detector#name()} and is used by
     * {@link io.unconquerable.intercept.decide.Decider} implementations to correlate results with
     * their originating detectors when multiple detectors are registered on the same
     * {@link io.unconquerable.intercept.Interceptor}.
     *
     * @return a non-null, non-empty detector name
     */
    String detectorName();
}
