package io.unconquerable.intercept.xgboost.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelSourceTest {

    @Nested
    class Construction {

        @Test
        void stores_location_modelId_and_version() {
            var source = new ModelSource("/models/fraud.ubj", "fraud-detector", "1.0.0");

            assertEquals("/models/fraud.ubj", source.location());
            assertEquals("fraud-detector", source.modelId());
            assertEquals("1.0.0", source.version());
        }
    }

    @Nested
    class Equality {

        @Test
        void two_instances_with_same_values_are_equal() {
            var a = new ModelSource("/path/model.ubj", "my-model", "2.1.0");
            var b = new ModelSource("/path/model.ubj", "my-model", "2.1.0");

            assertEquals(a, b);
        }

        @Test
        void instances_with_different_location_are_not_equal() {
            var a = new ModelSource("/path/a.ubj", "model", "1.0.0");
            var b = new ModelSource("/path/b.ubj", "model", "1.0.0");

            assertNotEquals(a, b);
        }

        @Test
        void instances_with_different_model_id_are_not_equal() {
            var a = new ModelSource("/path/model.ubj", "model-a", "1.0.0");
            var b = new ModelSource("/path/model.ubj", "model-b", "1.0.0");

            assertNotEquals(a, b);
        }

        @Test
        void instances_with_different_version_are_not_equal() {
            var a = new ModelSource("/path/model.ubj", "model", "1.0.0");
            var b = new ModelSource("/path/model.ubj", "model", "2.0.0");

            assertNotEquals(a, b);
        }
    }

    @Nested
    class StringRepresentation {

        @Test
        void toString_includes_all_fields() {
            var source = new ModelSource("/models/fraud.ubj", "fraud-detector", "1.0.0");
            var str = source.toString();

            assertTrue(str.contains("/models/fraud.ubj"));
            assertTrue(str.contains("fraud-detector"));
            assertTrue(str.contains("1.0.0"));
        }
    }
}