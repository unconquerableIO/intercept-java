package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.prediction.Prediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;
import ml.dmlc.xgboost4j.java.DMatrix;

/**
 * Strategy interface for converting the raw {@code float[][]} output of an XGBoost model
 * into a typed {@link Predictions} collection.
 *
 * <p>Each implementation is coupled to a specific XGBoost training objective (e.g.
 * {@code binary:logistic}, {@code multi:softprob}, {@code rank:pairwise}) and knows how to
 * interpret the raw score layout that objective produces.  Implementations may also apply
 * normalization via an injected {@link io.unconquerable.intercept.xgboost.normalizer.Normalizer}
 * where the model does not already do so internally.
 *
 * @param <T> the Java typeof a single normalized prediction value (e.g. {@code Double}, {@code Integer})
 * @param <P> the concrete {@link Prediction} wrapper type
 * @author Rizwan Idrees
 */
public interface PredictionsExtractor<T, P extends Prediction<T>> {

    /**
     * Extracts and normalizes predictions from the raw XGBoost output matrix.
     *
     * @param rawResult the raw score matrix returned by {@link ml.dmlc.xgboost4j.java.Booster#predict(DMatrix)};
     *                  each row corresponds to one sample; never {@code null}
     * @return a {@link Predictions} collection containing one prediction per input row;
     *         never {@code null}
     */
    Predictions<T, P> extract(@Nonnull float[][] rawResult);

}