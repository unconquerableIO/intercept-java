package io.unconquerable.intercept.functional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class EitherTest {

    // --- fixtures -----------------------------------------------------------

    private static final Either<String, Integer> LEFT  = Either.left("ok");
    private static final Either<String, Integer> RIGHT = Either.right(42);

    // ========================================================================

    @Nested
    class Construction {

        @Test
        void left_factory_produces_left_instance() {
            assertInstanceOf(Either.Left.class, Either.left("value"));
        }

        @Test
        void right_factory_produces_right_instance() {
            assertInstanceOf(Either.Right.class, Either.right("error"));
        }

        @Test
        void left_value_is_accessible() {
            assertEquals("ok", ((Either.Left<String, Integer>) LEFT).value());
        }

        @Test
        void right_value_is_accessible() {
            assertEquals(42, ((Either.Right<String, Integer>) RIGHT).value());
        }
    }

    // ========================================================================

    @Nested
    class Predicates {

        @Test
        void isLeft_is_true_for_left() {
            assertTrue(LEFT.isLeft());
        }

        @Test
        void isLeft_is_false_for_right() {
            assertFalse(RIGHT.isLeft());
        }

        @Test
        void isRight_is_true_for_right() {
            assertTrue(RIGHT.isRight());
        }

        @Test
        void isRight_is_false_for_left() {
            assertFalse(LEFT.isRight());
        }
    }

    // ========================================================================

    @Nested
    class Fold {

        @Test
        void applies_onLeft_when_left() {
            String result = LEFT.fold(v -> "left:" + v, e -> "right:" + e);
            assertEquals("left:ok", result);
        }

        @Test
        void applies_onRight_when_right() {
            String result = RIGHT.fold(v -> "left:" + v, e -> "right:" + e);
            assertEquals("right:42", result);
        }

        @Test
        void onRight_is_never_called_for_left() {
            boolean[] called = {false};
            LEFT.fold(v -> v, e -> { called[0] = true; return ""; });
            assertFalse(called[0]);
        }

        @Test
        void onLeft_is_never_called_for_right() {
            boolean[] called = {false};
            RIGHT.fold(v -> { called[0] = true; return ""; }, String::valueOf);
            assertFalse(called[0]);
        }
    }

    // ========================================================================

    @Nested
    class MapLeft {

        @Test
        void transforms_left_value() {
            Either<Integer, Integer> result = LEFT.mapLeft(String::length);
            assertEquals(2, ((Either.Left<Integer, Integer>) result).value());
        }

        @Test
        void leaves_right_unchanged() {
            Either<Integer, Integer> result = RIGHT.mapLeft(String::length);
            assertTrue(result.isRight());
            assertEquals(42, ((Either.Right<Integer, Integer>) result).value());
        }

        @Test
        void mapper_is_not_called_for_right() {
            boolean[] called = {false};
            RIGHT.mapLeft(v -> { called[0] = true; return v; });
            assertFalse(called[0]);
        }
    }

    // ========================================================================

    @Nested
    class MapRight {

        @Test
        void transforms_right_value() {
            Either<String, String> result = RIGHT.mapRight(n -> "error:" + n);
            assertEquals("error:42", ((Either.Right<String, String>) result).value());
        }

        @Test
        void leaves_left_unchanged() {
            Either<String, String> result = LEFT.mapRight(n -> "error:" + n);
            assertTrue(result.isLeft());
            assertEquals("ok", ((Either.Left<String, String>) result).value());
        }

        @Test
        void mapper_is_not_called_for_left() {
            boolean[] called = {false};
            LEFT.mapRight(v -> { called[0] = true; return v; });
            assertFalse(called[0]);
        }
    }

    // ========================================================================

    @Nested
    class FlatMapLeft {

        @Test
        void applies_mapper_and_returns_its_result_for_left() {
            Either<Integer, Integer> result = LEFT.flatMapLeft(v -> Either.left(v.length()));
            assertTrue(result.isLeft());
            assertEquals(2, ((Either.Left<Integer, Integer>) result).value());
        }

        @Test
        void mapper_can_return_right_turning_left_into_right() {
            Either<Integer, Integer> result = LEFT.flatMapLeft(v -> Either.right(99));
            assertTrue(result.isRight());
            assertEquals(99, ((Either.Right<Integer, Integer>) result).value());
        }

        @Test
        void leaves_right_unchanged() {
            Either<Integer, Integer> result = RIGHT.flatMapLeft(v -> Either.left(0));
            assertTrue(result.isRight());
            assertEquals(42, ((Either.Right<Integer, Integer>) result).value());
        }

        @Test
        void mapper_is_not_called_for_right() {
            boolean[] called = {false};
            RIGHT.flatMapLeft(v -> { called[0] = true; return Either.left(v); });
            assertFalse(called[0]);
        }
    }

    // ========================================================================

    @Nested
    class FlatMapRight {

        @Test
        void applies_mapper_and_returns_its_result_for_right() {
            Either<String, String> result = RIGHT.flatMapRight(n -> Either.right("error:" + n));
            assertTrue(result.isRight());
            assertEquals("error:42", ((Either.Right<String, String>) result).value());
        }

        @Test
        void mapper_can_return_left_turning_right_into_left() {
            Either<String, String> result = RIGHT.flatMapRight(n -> Either.left("recovered"));
            assertTrue(result.isLeft());
            assertEquals("recovered", ((Either.Left<String, String>) result).value());
        }

        @Test
        void leaves_left_unchanged() {
            Either<String, String> result = LEFT.flatMapRight(n -> Either.right("error"));
            assertTrue(result.isLeft());
            assertEquals("ok", ((Either.Left<String, String>) result).value());
        }

        @Test
        void mapper_is_not_called_for_left() {
            boolean[] called = {false};
            LEFT.flatMapRight(v -> { called[0] = true; return Either.right(v); });
            assertFalse(called[0]);
        }
    }

    // ========================================================================

    @Nested
    class Equality {

        @Test
        void two_lefts_with_same_value_are_equal() {
            assertEquals(Either.left("ok"), Either.left("ok"));
        }

        @Test
        void two_rights_with_same_value_are_equal() {
            assertEquals(Either.right(42), Either.right(42));
        }

        @Test
        void left_and_right_with_same_wrapped_value_are_not_equal() {
            assertNotEquals(Either.left(1), Either.right(1));
        }

        @Test
        void two_lefts_with_different_values_are_not_equal() {
            assertNotEquals(Either.left("a"), Either.left("b"));
        }
    }

    // ========================================================================

    @Nested
    class Chaining {

        @Test
        void map_then_fold_produces_expected_result_for_left() {
            String result = LEFT
                    .mapLeft(String::toUpperCase)
                    .fold(v -> "success:" + v, e -> "error:" + e);
            assertEquals("success:OK", result);
        }

        @Test
        void flatMap_chain_short_circuits_on_first_right() {
            Either<String, String> result = Either.<String, String>left("start")
                    .flatMapLeft(v -> Either.right("first error"))
                    .flatMapLeft(v -> Either.left("should not run"));
            assertTrue(result.isRight());
            assertEquals("first error", ((Either.Right<String, String>) result).value());
        }
    }
}