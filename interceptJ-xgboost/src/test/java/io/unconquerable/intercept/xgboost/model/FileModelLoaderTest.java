package io.unconquerable.intercept.xgboost.model;

import ml.dmlc.xgboost4j.java.Booster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileModelLoaderTest {

    private static Path validModelPath;

    @BeforeAll
    static void trainDummyModel() throws Exception {
        validModelPath = DummyModelFactory.createBinaryClassifierModel();
    }

    @Nested
    class Source {

        @Test
        void source_returns_the_configured_model_source() {
            var source = new ModelSource(validModelPath.toString(), "fraud-detector", "1.0.0");
            var loader = new FileModelLoader(source);

            assertEquals(source, loader.source());
        }
    }

    @Nested
    class Load {

        @Test
        void load_returns_non_null_booster_for_valid_model_file() {
            var source = new ModelSource(validModelPath.toString(), "fraud-detector", "1.0.0");
            Booster booster = new FileModelLoader(source).load();

            assertNotNull(booster);
        }

        @Test
        void load_throws_model_loader_exception_when_file_does_not_exist() {
            var source = new ModelSource("/nonexistent/path/model.ubj", "model", "1.0.0");

            assertThrows(ModelLoaderException.class, () -> new FileModelLoader(source).load());
        }

        @Test
        void load_throws_model_loader_exception_when_file_is_not_a_valid_model() throws Exception {
            Path corruptFile = Files.createTempFile("corrupt-model", ".ubj");
            corruptFile.toFile().deleteOnExit();
            Files.writeString(corruptFile, "this is not an xgboost model");

            var source = new ModelSource(corruptFile.toString(), "model", "1.0.0");

            assertThrows(ModelLoaderException.class, () -> new FileModelLoader(source).load());
        }

        @Test
        void load_can_be_called_multiple_times_on_the_same_loader() {
            var source = new ModelSource(validModelPath.toString(), "fraud-detector", "1.0.0");
            var loader = new FileModelLoader(source);

            assertNotNull(loader.load());
            assertNotNull(loader.load());
        }
    }

    @Nested
    class Equality {

        @Test
        void two_loaders_with_same_source_are_equal() {
            var source = new ModelSource(validModelPath.toString(), "fraud-detector", "1.0.0");

            assertEquals(new FileModelLoader(source), new FileModelLoader(source));
        }

        @Test
        void two_loaders_with_different_sources_are_not_equal() {
            var a = new FileModelLoader(new ModelSource("/path/a.ubj", "model", "1.0.0"));
            var b = new FileModelLoader(new ModelSource("/path/b.ubj", "model", "1.0.0"));

            assertNotEquals(a, b);
        }
    }
}