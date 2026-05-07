package io.unconquerable.intercept.xgboost.prediction;

import java.util.Collections;
import java.util.List;

/**
 * An ordered collection of {@link Prediction} values produced for a batch of input samples.
 *
 * <p>Wraps the list returned by a
 * {@link io.unconquerable.intercept.xgboost.prediction.extractors.PredictionsExtractor} and
 * exposes positional access, size, and an unmodifiable view of all predictions.  The order
 * of entries matches the row order of the {@code float[][]} matrix passed to the extractor.
 *
 * @param <T>           the type of the raw prediction value (e.g. {@code Double}, {@code Integer})
 * @param <P>           the concrete {@link Prediction} type
 * @param predictions   the ordered list of predictions; one entry per input sample
 * @author Rizwan Idrees
 */
public record Predictions<T, P extends Prediction<T>>(List<P> predictions) {

    /**
     * Returns the prediction at the given zero-based index.
     *
     * @param index zero-based position in the batch
     * @return the prediction at {@code index}
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public P at(int index) {
        return predictions.get(index);
    }

    /**
     * Returns the number of predictions in this collection.
     *
     * @return the batch size; always {@code >= 0}
     */
    public int size() {
        return predictions.size();
    }

    /**
     * Returns an unmodifiable view of all predictions in batch order.
     *
     * @return an unmodifiable {@link List} of all predictions; never {@code null}
     */
    public List<P> all() {
        return Collections.unmodifiableList(predictions);
    }
}
