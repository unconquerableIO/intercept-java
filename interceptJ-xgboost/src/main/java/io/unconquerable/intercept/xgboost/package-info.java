/**
 * XGBoost inference module for the interceptJ library.
 *
 * <p>This module provides everything needed to load an XGBoost model and run batch inference
 * against it in a functional, exception-free style.  The key entry points are:
 *
 * <ul>
 *   <li>{@link io.unconquerable.intercept.xgboost.model.ModelLoader} — strategy interface for
 *       loading a {@link ml.dmlc.xgboost4j.java.Booster} from a
 *       {@link io.unconquerable.intercept.xgboost.model.ModelSource}</li>
 *   <li>{@link io.unconquerable.intercept.xgboost.predictor.XGBoostPredictor} — runs inference
 *       and returns {@code Either<? extends Prediction, Error>}</li>
 *   <li>{@link io.unconquerable.intercept.xgboost.predictor.Predictor} — fluent builder that
 *       wires feature extraction, prediction, and result retrieval</li>
 *   <li>{@link io.unconquerable.intercept.xgboost.prediction.extractors.PredictionsExtractor} —
 *       objective-specific converters from raw {@code float[][]} to typed
 *       {@link io.unconquerable.intercept.xgboost.prediction.Predictions}</li>
 *   <li>{@link io.unconquerable.intercept.xgboost.normalizer.Normalizer} — post-prediction
 *       normalisation (sigmoid, min-max, ranking)</li>
 * </ul>
 *
 * <p>Typical usage:
 * <pre>{@code
 * XGBoostPredictor predictor = new XGBoostPredictor(new FileModelLoader(source));
 *
 * Either<? extends Prediction<?>, Error> result = Predictor
 *     .predictor(predictor, input)
 *     .extractFeatures(featureExtractor)
 *     .predict()
 *     .result();
 *
 * result.fold(
 *     prediction -> extractor.extract((float[][]) prediction.value()),
 *     error      -> log(error.message())
 * );
 * }</pre>
 *
 * @author Rizwan Idrees
 */
package io.unconquerable.intercept.xgboost;