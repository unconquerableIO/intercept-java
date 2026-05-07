package io.unconquerable.intercept.xgboost.prediction;

public record DefaultPrediction<T>(T value) implements Prediction<T> {
}
