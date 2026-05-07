package io.unconquerable.intercept.xgboost.model;

import ml.dmlc.xgboost4j.java.Booster;

/**
 * Strategy interface for loading an XGBoost {@link Booster} from a {@link ModelSource}.
 *
 * <p>Implementations are responsible for resolving the model artefact (file system, object
 * storage, model registry, etc.) and deserializing it into a ready-to-use {@code Booster}.
 *
 * @author Rizwan Idrees
 */
public interface ModelLoader {

    /**
     * Returns the source descriptor that identifies the model artefact to be loaded.
     *
     * @return the {@link ModelSource} for this loader; never {@code null}
     */
    ModelSource source();

    /**
     * Loads and returns an XGBoost {@link Booster} from the configured source.
     *
     * @return the loaded {@code Booster}; never {@code null}
     * @throws ModelLoaderException if the model cannot be read or deserialised
     */
    Booster load() throws ModelLoaderException;
}
