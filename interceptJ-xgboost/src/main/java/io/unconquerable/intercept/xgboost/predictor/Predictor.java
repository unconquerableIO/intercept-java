package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Prediction;
import io.unconquerable.intercept.xgboost.prediction.PredictionError;
import io.unconquerable.intercept.xgboost.prediction.Outcome;
import io.unconquerable.intercept.xgboost.prediction.decoder.PredictionsDecoder;
import ml.dmlc.xgboost4j.java.DMatrix;

import java.util.Optional;
import java.util.function.Function;

public class Predictor<T> {

    private static final PredictionError NO_FEATURES_ERROR = PredictionError.of("No feature extractor is defined");

    private final XGBoostPredictor model;
    private final T type;
    private Function<T, DMatrix> featureExtractor;
    private Outcome<DefaultPrediction<float[][]>, PredictionError> result;

    private Predictor(XGBoostPredictor model, T type) {
        this.model = model;
        this.type = type;
    }

    public static <T> Predictor<T> predictor(XGBoostPredictor model, T type) {
        return new Predictor<>(model, type);
    }

    public Predictor<T> translate(Function<T, DMatrix> toFeatures) {
        this.featureExtractor = toFeatures;
        return this;
    }

    public <OT, P  extends Prediction<OT>> Prediction<OT>  predict(PredictionsDecoder<OT,P> decoder) {
        this.result = Optional.ofNullable(featureExtractor)
                .map(fe -> fe.apply(type))
                .map(model::predict)
                .orElseGet(() -> Outcome.failure(NO_FEATURES_ERROR));
        return null;
    }



}
