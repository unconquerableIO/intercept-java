package io.unconquerable.intercept.xgboost.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ModelLoaderExceptionTest {

    @Nested
    class CauseOnlyConstructor {

        @Test
        void wraps_the_original_cause() {
            var cause = new IOException("disk read failed");
            var ex = new ModelLoaderException(cause);

            assertSame(cause, ex.getCause());
        }

        @Test
        void message_is_derived_from_cause() {
            var cause = new IOException("disk read failed");
            var ex = new ModelLoaderException(cause);

            assertTrue(ex.getMessage().contains("disk read failed"));
        }
    }

    @Nested
    class MessageAndCauseConstructor {

        @Test
        void sets_message_and_cause() {
            var cause = new IOException("disk read failed");
            var ex = new ModelLoaderException("could not load model", cause);

            assertEquals("could not load model", ex.getMessage());
            assertSame(cause, ex.getCause());
        }
    }

    @Nested
    class TypeHierarchy {

        @Test
        void is_a_runtime_exception() {
            assertInstanceOf(RuntimeException.class, new ModelLoaderException(new IOException()));
        }
    }
}