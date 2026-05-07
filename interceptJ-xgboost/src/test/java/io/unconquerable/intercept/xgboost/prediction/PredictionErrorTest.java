package io.unconquerable.intercept.xgboost.prediction;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PredictionErrorTest {

    @Nested
    class OfMessage {

        @Test
        void sets_message() {
            assertEquals("model not found", PredictionError.of("model not found").message());
        }

        @Test
        void cause_is_null() {
            assertNull(PredictionError.of("model not found").cause());
        }
    }

    @Nested
    class OfThrowable {

        @Test
        void extracts_message_from_cause() {
            var cause = new IOException("disk read failed");
            assertEquals("disk read failed", PredictionError.of(cause).message());
        }

        @Test
        void preserves_cause() {
            var cause = new IOException("disk read failed");
            assertSame(cause, PredictionError.of(cause).cause());
        }
    }

    @Nested
    class ErrorContract {

        @Test
        void implements_error_interface() {
            assertInstanceOf(Error.class, PredictionError.of("failure"));
        }

        @Test
        void message_from_interface_matches_record_accessor() {
            Error error = PredictionError.of("failure");
            assertEquals("failure", error.message());
        }
    }

    @Nested
    class Equality {

        @Test
        void two_instances_with_same_message_and_null_cause_are_equal() {
            assertEquals(PredictionError.of("oops"), PredictionError.of("oops"));
        }

        @Test
        void two_instances_with_different_messages_are_not_equal() {
            assertNotEquals(PredictionError.of("a"), PredictionError.of("b"));
        }
    }
}