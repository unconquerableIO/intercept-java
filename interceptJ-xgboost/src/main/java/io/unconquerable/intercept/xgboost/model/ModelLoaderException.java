package io.unconquerable.intercept.xgboost.model;

/**
 * Unchecked exception thrown when a {@link ModelLoader} fails to load an XGBoost model.
 *
 * <p>Wraps lower-level checked exceptions (e.g. {@link java.io.IOException},
 * {@link ml.dmlc.xgboost4j.java.XGBoostError}) so callers do not need to declare them,
 * while still preserving the original cause for diagnosis.
 *
 * @author Rizwan Idrees
 */
public class ModelLoaderException extends RuntimeException {

    /**
     * Creates a new exception wrapping the given cause.
     *
     * @param cause the underlying exception that triggered this failure
     */
    public ModelLoaderException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with an explicit message and a cause.
     *
     * @param message a human-readable description of the failure
     * @param cause   the underlying exception that triggered this failure
     */
    public ModelLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
