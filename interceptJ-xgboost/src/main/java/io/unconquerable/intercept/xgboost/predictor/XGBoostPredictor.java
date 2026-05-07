package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.functional.Either;
import io.unconquerable.intercept.xgboost.model.ModelLoader;
import io.unconquerable.intercept.xgboost.prediction.*;
import io.unconquerable.intercept.xgboost.prediction.Error;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;

public class XGBoostPredictor {

    private final Booster booster;

    public XGBoostPredictor(ModelLoader loader) {
        this.booster = loader.load();
    }

    public Either<? extends Prediction<float[][]>, Error> predict(DMatrix matrix) {
        try {
            return Either.left(new DefaultPrediction<>(booster.predict(matrix)));
        } catch (Exception e) {
            return Either.right(PredictionError.of(e));
        }
    }

}
