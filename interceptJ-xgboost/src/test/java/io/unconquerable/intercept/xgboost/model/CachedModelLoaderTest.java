package io.unconquerable.intercept.xgboost.model;

import ml.dmlc.xgboost4j.java.Booster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class CachedModelLoaderTest {

    private static Path modelPath;

    @BeforeAll
    static void trainDummyModel() throws Exception {
        modelPath = DummyModelFactory.createBinaryClassifierModel();
    }

    // ── Test doubles ──────────────────────────────────────────────────────────

    /** Wraps a real loader and counts how many times load() is called. */
    static class CountingLoader implements ModelLoader {

        private final ModelLoader delegate;
        int loadCount = 0;

        CountingLoader(ModelLoader delegate) {
            this.delegate = delegate;
        }

        @Override
        public ModelSource source() {
            return delegate.source();
        }

        @Override
        public Booster load() throws ModelLoaderException {
            loadCount++;
            return delegate.load();
        }
    }

    /** Fails on the first call, then delegates normally on subsequent calls. */
    static class FailOnceThenSucceedLoader implements ModelLoader {

        private final ModelLoader delegate;
        private boolean failed = false;
        int loadCount = 0;

        FailOnceThenSucceedLoader(ModelLoader delegate) {
            this.delegate = delegate;
        }

        @Override
        public ModelSource source() {
            return delegate.source();
        }

        @Override
        public Booster load() throws ModelLoaderException {
            loadCount++;
            if (!failed) {
                failed = true;
                throw new ModelLoaderException(new RuntimeException("simulated load failure"));
            }
            return delegate.load();
        }
    }

    private CountingLoader countingLoader() {
        var source = new ModelSource(modelPath.toString(), "dummy", "1.0.0");
        return new CountingLoader(new FileModelLoader(source));
    }

    // ── Source delegation ─────────────────────────────────────────────────────

    @Nested
    class Source {

        @Test
        void delegates_source_to_underlying_loader() {
            var inner = countingLoader();
            var cached = new CachedModelLoader(inner);

            assertEquals(inner.source(), cached.source());
        }
    }

    // ── Caching behaviour ─────────────────────────────────────────────────────

    @Nested
    class Caching {

        @Test
        void load_calls_delegate_on_first_invocation() {
            var inner = countingLoader();
            var cached = new CachedModelLoader(inner);

            cached.load();

            assertEquals(1, inner.loadCount);
        }

        @Test
        void load_returns_non_null_booster() {
            var cached = new CachedModelLoader(countingLoader());

            assertNotNull(cached.load());
        }

        @Test
        void second_call_returns_same_booster_instance() {
            var cached = new CachedModelLoader(countingLoader());

            Booster first  = cached.load();
            Booster second = cached.load();

            assertSame(first, second);
        }

        @Test
        void delegate_is_called_exactly_once_across_multiple_load_calls() {
            var inner  = countingLoader();
            var cached = new CachedModelLoader(inner);

            cached.load();
            cached.load();
            cached.load();

            assertEquals(1, inner.loadCount);
        }
    }

    // ── Failure and retry ─────────────────────────────────────────────────────

    @Nested
    class FailureAndRetry {

        @Test
        void load_propagates_exception_from_delegate() {
            var source = new ModelSource("/nonexistent/model.ubj", "dummy", "1.0.0");
            var cached = new CachedModelLoader(new FileModelLoader(source));

            assertThrows(ModelLoaderException.class, cached::load);
        }

        @Test
        void nothing_is_cached_after_a_failed_load() {
            var inner  = new FailOnceThenSucceedLoader(new FileModelLoader(
                    new ModelSource(modelPath.toString(), "dummy", "1.0.0")));
            var cached = new CachedModelLoader(inner);

            assertThrows(ModelLoaderException.class, cached::load);

            // second call must reach the delegate again and succeed
            assertNotNull(cached.load());
            assertEquals(2, inner.loadCount);
        }

        @Test
        void successful_load_after_failure_is_then_cached() {
            var inner  = new FailOnceThenSucceedLoader(new FileModelLoader(
                    new ModelSource(modelPath.toString(), "dummy", "1.0.0")));
            var cached = new CachedModelLoader(inner);

            assertThrows(ModelLoaderException.class, cached::load);
            Booster second = cached.load();
            Booster third  = cached.load();

            assertSame(second, third);
            assertEquals(2, inner.loadCount); // third call must not reach the delegate
        }
    }

    // ── Thread safety ─────────────────────────────────────────────────────────

    @Nested
    class ThreadSafety {

        @Test
        void delegate_is_called_exactly_once_under_concurrent_load() {
            var inner  = countingLoader();
            var cached = new CachedModelLoader(inner);

            int threads = 20;
            var latch   = new CountDownLatch(1);
            var results = new CopyOnWriteArrayList<Booster>();

            try (var executor = Executors.newFixedThreadPool(threads)) {
                for (int i = 0; i < threads; i++) {
                    executor.submit(() -> {
                        try {
                            latch.await();
                            results.add(cached.load());
                        } catch (Exception ignored) {}
                    });
                }
                latch.countDown(); // release all threads simultaneously
            }

            assertEquals(threads, results.size());
            assertEquals(1, inner.loadCount);
            assertTrue(results.stream().allMatch(b -> b == results.getFirst()),
                    "All threads must receive the same Booster instance");
        }
    }
}