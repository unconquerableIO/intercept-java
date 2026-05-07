package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.Prediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

public interface PredictionsExtractor<T, P extends Prediction<T>> {

    Predictions<T, P> extract(@Nonnull float[][] rawResult);

}