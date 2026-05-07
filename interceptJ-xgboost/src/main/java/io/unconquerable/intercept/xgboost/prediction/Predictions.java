package io.unconquerable.intercept.xgboost.prediction;

import java.util.Collections;
import java.util.List;

public record Predictions<T, P extends Prediction<T>>(List<P> predictions) {

    public P at(int index) {
        return predictions.get(index);
    }

    public int size() {
        return predictions.size();
    }

    public List<P> all() {
        return Collections.unmodifiableList(predictions);
    }
}
