package io.unconquerable.intercept.xgboost.inference;

/**
 * Marker interface for the outcome of interpreting an XGBoost prediction.
 *
 * <p>Implement this interface to represent the domain-specific choices your application
 * can make after running inference — for example, {@code BLOCK}, {@code ALLOW}, or
 * {@code REVIEW}.  Instances are carried by an {@link Inference} and returned from an
 * {@link io.unconquerable.intercept.xgboost.interpreter.Interpreter}.
 *
 * @author Rizwan Idrees
 */
public interface Decision {
}
