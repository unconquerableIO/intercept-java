package io.unconquerable.intercept.xgboost.prediction.extractors;

import io.unconquerable.intercept.xgboost.normalizer.Normalizer;
import io.unconquerable.intercept.xgboost.prediction.DefaultPrediction;
import io.unconquerable.intercept.xgboost.prediction.Predictions;
import jakarta.annotation.Nonnull;

import java.util.stream.IntStream;

/**
 * Objective rank:pairwise
 * XGBoost outputs ranking scores that are only meaningful relative to
 * each other within a single inference batch.
 */
public record RankingObjectiveExtractor(
        Normalizer<float[], double[]> normalizer) implements PredictionsExtractor<Double, DefaultPrediction<Double>> {

    @Override
    public Predictions<Double, DefaultPrediction<Double>> extract(@Nonnull float[][] rawResult) {
        float[] flat = new float[rawResult.length];
        for (int i = 0; i < rawResult.length; i++){
            flat[i] = rawResult[i][0];
        }
        double[] normalised = normalizer.normalize(flat);
        return new Predictions<>(IntStream.range(0, rawResult.length)
                .mapToObj(i -> new DefaultPrediction<>(normalised[i]))
                .toList());
    }
}