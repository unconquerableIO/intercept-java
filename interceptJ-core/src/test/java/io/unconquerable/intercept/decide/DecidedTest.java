package io.unconquerable.intercept.decide;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.unconquerable.intercept.decide.Decided.*;
import static io.unconquerable.intercept.decide.Decided.Type.*;
import static org.junit.jupiter.api.Assertions.*;

class DecidedTest {

    private static final DecisionDetail DETAIL = new DecisionDetail() {};

    // =========================================================================

    @Nested
    class FactoryMethods {

        @Test
        void decidedToBlock_creates_DecidedToBlock() {
            assertInstanceOf(DecidedToBlock.class, decidedToBlock());
        }

        @Test
        void decidedToProceed_creates_DecidedToProceed() {
            assertInstanceOf(DecidedToProceed.class, decidedToProceed());
        }

        @Test
        void decidedToChallenge_creates_DecidedToChallenge() {
            assertInstanceOf(DecidedToChallenge.class, decidedToChallenge());
        }

        @Test
        void decidedToDefer_creates_DecidedToDefer() {
            assertInstanceOf(DecidedToDefer.class, decidedToDefer());
        }

        @Test
        void factory_methods_with_detail_pass_detail_through() {
            assertEquals(Optional.of(DETAIL), decidedToBlock(DETAIL).details());
            assertEquals(Optional.of(DETAIL), decidedToProceed(DETAIL).details());
            assertEquals(Optional.of(DETAIL), decidedToChallenge(DETAIL).details());
            assertEquals(Optional.of(DETAIL), decidedToDefer(DETAIL).details());
        }

        @Test
        void factory_methods_without_detail_produce_empty_details() {
            assertEquals(Optional.empty(), decidedToBlock().details());
            assertEquals(Optional.empty(), decidedToProceed().details());
            assertEquals(Optional.empty(), decidedToChallenge().details());
            assertEquals(Optional.empty(), decidedToDefer().details());
        }
    }

    // =========================================================================

    @Nested
    class Types {

        @Test
        void DecidedToBlock_type_is_BLOCK() {
            assertEquals(BLOCK, decidedToBlock().type());
        }

        @Test
        void DecidedToProceed_type_is_PROCEED() {
            assertEquals(PROCEED, decidedToProceed().type());
        }

        @Test
        void DecidedToChallenge_type_is_CHALLENGE() {
            assertEquals(CHALLENGE, decidedToChallenge().type());
        }

        @Test
        void DecidedToDefer_type_is_DEFER() {
            assertEquals(DEFER, decidedToDefer().type());
        }
    }

    // =========================================================================

    @Nested
    class ConvenienceMethods {

        @Test
        void toBlock_is_true_only_for_BLOCK() {
            assertTrue(decidedToBlock().toBlock());
            assertFalse(decidedToProceed().toBlock());
            assertFalse(decidedToChallenge().toBlock());
            assertFalse(decidedToDefer().toBlock());
        }

        @Test
        void toProceed_is_true_only_for_PROCEED() {
            assertTrue(decidedToProceed().toProceed());
            assertFalse(decidedToBlock().toProceed());
            assertFalse(decidedToChallenge().toProceed());
            assertFalse(decidedToDefer().toProceed());
        }

        @Test
        void toChallenge_is_true_only_for_CHALLENGE() {
            assertTrue(decidedToChallenge().toChallenge());
            assertFalse(decidedToBlock().toChallenge());
            assertFalse(decidedToProceed().toChallenge());
            assertFalse(decidedToDefer().toChallenge());
        }

        @Test
        void toDefer_is_true_only_for_DEFER() {
            assertTrue(decidedToDefer().toDefer());
            assertFalse(decidedToBlock().toDefer());
            assertFalse(decidedToProceed().toDefer());
            assertFalse(decidedToChallenge().toDefer());
        }
    }

    // =========================================================================

    @Nested
    class Details {

        @Test
        void details_returns_present_when_detail_provided() {
            assertTrue(decidedToBlock(DETAIL).details().isPresent());
        }

        @Test
        void details_returns_same_instance_that_was_provided() {
            Optional<DecisionDetail> details = decidedToBlock(DETAIL).details();
            assertTrue(details.isPresent());
            assertSame(DETAIL, details.get());
        }

        @Test
        void details_returns_empty_when_null_is_passed_to_canonical_constructor() {
            var decided = new DecidedToBlock(BLOCK, null);
            assertEquals(Optional.empty(), decided.details());
        }

        @Test
        void details_with_null_passed_to_compact_constructor_returns_empty() {
            assertEquals(Optional.empty(), new DecidedToProceed(null).details());
            assertEquals(Optional.empty(), new DecidedToChallenge(null).details());
            assertEquals(Optional.empty(), new DecidedToDefer(null).details());
        }
    }

    // =========================================================================

    @Nested
    class RecordEquality {

        @Test
        void two_block_verdicts_without_detail_are_equal() {
            assertEquals(decidedToBlock(), decidedToBlock());
        }

        @Test
        void block_and_proceed_verdicts_are_not_equal() {
            assertNotEquals(decidedToBlock(), decidedToProceed());
        }

        @Test
        void verdicts_with_and_without_detail_are_not_equal() {
            assertNotEquals(decidedToBlock(), decidedToBlock(DETAIL));
        }
    }
}