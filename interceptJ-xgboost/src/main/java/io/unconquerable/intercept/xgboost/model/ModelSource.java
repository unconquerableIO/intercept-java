package io.unconquerable.intercept.xgboost.model;

import jakarta.annotation.Nonnull;

/**
 * Immutable descriptor that identifies an XGBoost model artefact.
 *
 * <p>A {@code ModelSource} captures the three coordinates needed to unambiguously locate a
 * model: where it physically lives ({@link #location}), what logical model it represents
 * ({@link #modelId}), and which revision is being used ({@link #version}).  It is consumed
 * by {@link ModelLoader} implementations to resolve and load the corresponding
 * {@link ml.dmlc.xgboost4j.java.Booster}.
 *
 * @param location the path or URI that points to the serialised model artefact
 * @param modelId  a logical identifier for the model (e.g. {@code "fraud-detector"})
 * @param version  the version label for the model artefact (e.g. {@code "1.3.0"})
 * @author Rizwan Idrees
 */
public record ModelSource(@Nonnull String location,
                          @Nonnull String modelId,
                          @Nonnull String version) {
}
