package io.unconquerable.intercept.xgboost.inference;

/**
 * The result produced by an {@link io.unconquerable.intercept.xgboost.interpreter.Interpreter}
 * after evaluating an XGBoost prediction.
 *
 * <p>Wraps a {@link Decision} so that interpreters can return rich objects — carrying
 * additional context alongside the decision itself — while keeping the pipeline's return
 * type uniform.  Implement this interface to attach metadata such as confidence scores,
 * audit trails, or explanations to the decision.
 *
 * @param <D> the concrete {@link Decision} type this inference carries
 * @author Rizwan Idrees
 */
public interface Inference<D extends Decision> {

    /**
     * Returns the decision reached by the interpreter.
     *
     * @return the decision; never {@code null}
     */
    D decision();

}
