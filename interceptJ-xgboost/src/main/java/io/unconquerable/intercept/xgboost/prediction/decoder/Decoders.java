package io.unconquerable.intercept.xgboost.prediction.decoder;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;

/**
 * Factory class for all built-in {@link PredictionsDecoder} implementations.
 *
 * <p>Each method corresponds to a specific XGBoost training objective and returns the
 * decoder suited to interpret its raw {@code float[][]} output.  Use this class as the
 * primary entry point when wiring a decoder into the prediction pipeline:
 *
 * <pre>{@code
 * Predictor.predictor(model, request)
 *     .translate(req -> buildMatrix(req))
 *     .predict()
 *     .decode(Decoders.binaryLogistic())
 *     ...
 * }</pre>
 *
 * @author Rizwan Idrees
 */
public final class Decoders {

    private Decoders() {
    }

    /**
     * Returns a decoder for the XGBoost {@code binary:logistic} objective.
     *
     * <p>The model outputs calibrated probabilities directly, so no normalizer is needed.
     *
     * @return a {@link BinaryLogisticObjectiveDecoder}
     */
    public static BinaryLogisticObjectiveDecoder binaryLogistic() {
        return new BinaryLogisticObjectiveDecoder();
    }

    /**
     * Returns a decoder for the XGBoost {@code binary:logitraw} objective.
     *
     * <p>The model outputs raw log-odds.  The supplied {@code normalizer} (typically a
     * {@link io.unconquerable.intercept.xgboost.normalizer.SigmoidNormalizer}) is applied
     * to each score to convert it to a probability in {@code [0, 1]}.
     *
     * @param normalizer normalizer applied to each raw log-odds score; never {@code null}
     * @return a {@link BinaryLogitrawObjectiveDecoder}
     */
    public static BinaryLogitrawObjectiveDecoder binaryLogitraw(Normalizer<Float, Double> normalizer) {
        return new BinaryLogitrawObjectiveDecoder(normalizer);
    }

    /**
     * Returns a decoder for the XGBoost {@code multi:softmax} objective.
     *
     * <p>The model outputs a single integer class label per sample.  The label is compared
     * against {@code targetClassIndex}: a match yields {@code 1}, otherwise {@code 0}.
     *
     * @param targetClassIndex the class label to treat as a positive match
     * @return a {@link MultiSoftMaxObjectiveDecoder}
     */
    public static MultiSoftMaxObjectiveDecoder multiSoftMax(int targetClassIndex) {
        return new MultiSoftMaxObjectiveDecoder(targetClassIndex);
    }

    /**
     * Returns a decoder for the XGBoost {@code multi:softprob} objective.
     *
     * <p>The model outputs a probability distribution over all classes.  The probability at
     * {@code targetClassIndex} is extracted as the prediction value for each sample.
     *
     * @param targetClassIndex the class index whose probability is used as the prediction value
     * @return a {@link MultiSoftProbObjectiveDecoder}
     */
    public static MultiSoftProbObjectiveDecoder multiSoftProb(int targetClassIndex) {
        return new MultiSoftProbObjectiveDecoder(targetClassIndex);
    }

    /**
     * Returns a decoder for the XGBoost {@code reg:absoluteerror} objective.
     *
     * <p>The model outputs a raw regression value.  The supplied {@code normalizer} is applied
     * to each score before it is wrapped in a prediction.
     *
     * @param normalizer normalizer applied to each raw regression score; never {@code null}
     * @return a {@link RegressionAbsoluteErrorObjectiveDecoder}
     */
    public static RegressionAbsoluteErrorObjectiveDecoder regressionAbsoluteError(Normalizer<Float, Double> normalizer) {
        return new RegressionAbsoluteErrorObjectiveDecoder(normalizer);
    }

    /**
     * Returns a decoder for the XGBoost {@code reg:logistic} objective.
     *
     * <p>The model outputs a probability, but the supplied {@code normalizer} can apply
     * further transformation (e.g. min-max scaling) before the value is wrapped in a prediction.
     *
     * @param normalizer normalizer applied to each raw score; never {@code null}
     * @return a {@link RegressionLogisticObjectiveDecoder}
     */
    public static RegressionLogisticObjectiveDecoder regressionLogistic(Normalizer<Float, Double> normalizer) {
        return new RegressionLogisticObjectiveDecoder(normalizer);
    }

    /**
     * Returns a decoder for the XGBoost {@code reg:squarederror} objective.
     *
     * <p>The model outputs a raw regression value.  The supplied {@code normalizer} is applied
     * to each score before it is wrapped in a prediction.
     *
     * @param normalizer normalizer applied to each raw regression score; never {@code null}
     * @return a {@link RegressionSquaredErrorObjectiveDecoder}
     */
    public static RegressionSquaredErrorObjectiveDecoder regressionSquaredError(Normalizer<Float, Double> normalizer) {
        return new RegressionSquaredErrorObjectiveDecoder(normalizer);
    }

    /**
     * Returns a decoder for the XGBoost ranking objectives ({@code rank:pairwise},
     * {@code rank:ndcg}, {@code rank:map}).
     *
     * <p>The model outputs a score per sample.  The supplied {@code normalizer} transforms
     * the full score array (e.g. via
     * {@link io.unconquerable.intercept.xgboost.normalizer.RankingNormalizer}) before each
     * value is wrapped in a prediction.
     *
     * @param normalizer normalizer applied to the full array of raw ranking scores; never {@code null}
     * @return a {@link RankingObjectiveDecoder}
     */
    public static RankingObjectiveDecoder rankingDecoder(Normalizer<float[], double[]> normalizer) {
        return new RankingObjectiveDecoder(normalizer);
    }
}