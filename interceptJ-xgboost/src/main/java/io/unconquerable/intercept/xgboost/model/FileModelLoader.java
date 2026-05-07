package io.unconquerable.intercept.xgboost.model;

import jakarta.annotation.Nonnull;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A {@link ModelLoader} that reads an XGBoost model from the local file system.
 *
 * <p>The model artifact is located via {@link ModelSource#location()}, which must be a valid
 * path to a serialized XGBoost binary model file.  Any I/O or XGBoost deserialisation failure
 * is wrapped in a {@link ModelLoaderException}.
 *
 * @param source the {@link ModelSource} whose {@code location} points to the model file
 * @author Rizwan Idrees
 */
public record FileModelLoader(@Nonnull ModelSource source) implements ModelLoader {

    /**
     * Loads the XGBoost {@link Booster} from the file identified by {@link ModelSource#location()}.
     *
     * @return the deserialized {@code Booster}; never {@code null}
     * @throws ModelLoaderException if the file cannot be opened or the model cannot be parsed
     */
    @Override
    public Booster load() throws ModelLoaderException {
        try {
            return XGBoost.loadModel(new FileInputStream(source.location()));
        } catch (XGBoostError | IOException e) {
            throw new ModelLoaderException(e);
        }
    }
}
