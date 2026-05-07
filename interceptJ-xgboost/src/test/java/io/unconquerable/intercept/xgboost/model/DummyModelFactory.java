package io.unconquerable.intercept.xgboost.model;

import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test utility that trains and saves a minimal XGBoost model to a temporary file.
 *
 * <p>The produced model is a single-round binary classifier trained on four samples.
 * It is only suitable for verifying load/predict mechanics — not for evaluating accuracy.
 */
public final class DummyModelFactory {

    private DummyModelFactory() {}

    public static Path createBinaryClassifierModel() throws XGBoostError, IOException {
        float[] data = {1f, 2f, 3f, 4f};
        DMatrix matrix = new DMatrix(data, 2, 2, Float.NaN);
        matrix.setLabel(new float[]{0f, 1f});

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("objective", "binary:logistic");
        params.put("max_depth", 2);
        params.put("nthread", 1);

        var watches = new LinkedHashMap<String, DMatrix>();
        var booster = XGBoost.train(matrix, params, 1, watches, null, null);

        Path modelPath = Files.createTempFile("xgboost-dummy", ".ubj");
        modelPath.toFile().deleteOnExit();
        booster.saveModel(modelPath.toString());
        return modelPath;
    }
}