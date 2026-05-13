package io.unconquerable.intercept.xgboost.model;

import ml.dmlc.xgboost4j.java.Booster;

/**
 * A {@link ModelLoader} decorator that loads the {@link Booster} on first use and returns the
 * same instance on every subsequent call.
 *
 * <p>Delegates the initial load to the wrapped {@link ModelLoader}.  If loading fails the
 * exception propagates and nothing is cached — the next call will retry.  Once a {@code Booster}
 * is successfully loaded it is held for the lifetime of this instance.
 *
 * <p>Thread-safe: uses double-checked locking on a {@code volatile} field so concurrent callers
 * never load the model more than once.
 *
 * <p>Typical usage — wrap any loader before passing it to {@link
 * io.unconquerable.intercept.xgboost.predictor.XGBoostPredictor}:
 * <pre>{@code
 * ModelLoader loader = new CachedModelLoader(new FileModelLoader(source));
 * XGBoostPredictor predictor = new XGBoostPredictor(loader);
 * }</pre>
 *
 * @author Rizwan Idrees
 */
public final class CachedModelLoader implements ModelLoader {

    private final ModelLoader delegate;
    private volatile Booster cached;

    /**
     * Creates a {@code CachedModelLoader} that delegates loading to {@code delegate}.
     *
     * @param delegate the underlying loader; never {@code null}
     */
    public CachedModelLoader(ModelLoader delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the {@link ModelSource} from the underlying loader.
     *
     * @return the source; never {@code null}
     */
    @Override
    public ModelSource source() {
        return delegate.source();
    }

    /**
     * Returns the cached {@link Booster}, loading it from the delegate on the first call.
     *
     * @return the {@code Booster}; never {@code null}
     * @throws ModelLoaderException if the delegate fails to load the model; nothing is cached
     *                              and the next call will retry
     */
    @Override
    public Booster load() throws ModelLoaderException {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = delegate.load();
                }
            }
        }
        return cached;
    }
}