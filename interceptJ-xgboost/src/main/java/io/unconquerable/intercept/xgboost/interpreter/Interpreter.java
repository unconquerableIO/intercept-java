package io.unconquerable.intercept.xgboost.interpreter;

import io.unconquerable.intercept.xgboost.inference.Decision;
import io.unconquerable.intercept.xgboost.inference.Inference;

/**
 * Strategy for converting a typed input into an {@link Inference} carrying a {@link Decision}.
 *
 * <p>Used at the terminal stage of the prediction pipeline to map either a decoded
 * {@link io.unconquerable.intercept.xgboost.prediction.Predictions} batch or a
 * {@link io.unconquerable.intercept.xgboost.prediction.PredictionError} into a domain decision.
 *
 * @param <I> the input type (e.g. {@code Predictions<OT, P>} or {@code PredictionError})
 * @param <D> the concrete {@link Decision} type this interpreter produces
 * @author Rizwan Idrees
 */
@FunctionalInterface
public interface Interpreter<I, D extends Decision> {

    /**
     * Maps {@code input} to an {@link Inference} carrying a {@link Decision}.
     *
     * @param input the value to interpret; never {@code null}
     * @return the inference result; never {@code null}
     */
    Inference<D> interpret(I input);

}