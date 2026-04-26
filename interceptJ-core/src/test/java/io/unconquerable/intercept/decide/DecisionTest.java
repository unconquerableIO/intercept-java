package io.unconquerable.intercept.decide;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.unconquerable.intercept.decide.Decided.*;
import static org.junit.jupiter.api.Assertions.*;

class DecisionTest {

    // =========================================================================

    @Nested
    class MatchingHandler {

        @Test
        void onBlock_supplier_is_invoked_for_block_verdict() {
            var result = new Decision<String>(decidedToBlock())
                    .onBlock(() -> "blocked")
                    .result();

            assertEquals(Optional.of("blocked"), result);
        }

        @Test
        void onProceed_supplier_is_invoked_for_proceed_verdict() {
            var result = new Decision<String>(decidedToProceed())
                    .onProceed(() -> "proceed")
                    .result();

            assertEquals(Optional.of("proceed"), result);
        }

        @Test
        void onChallenge_supplier_is_invoked_for_challenge_verdict() {
            var result = new Decision<String>(decidedToChallenge())
                    .onChallenge(() -> "challenge")
                    .result();

            assertEquals(Optional.of("challenge"), result);
        }

        @Test
        void onDefer_supplier_is_invoked_for_defer_verdict() {
            var result = new Decision<String>(decidedToDefer())
                    .onDefer(() -> "deferred")
                    .result();

            assertEquals(Optional.of("deferred"), result);
        }
    }

    // =========================================================================

    @Nested
    class NonMatchingHandlers {

        @Test
        void non_matching_suppliers_are_not_invoked_on_block_verdict() {
            boolean[] called = {false, false, false};

            new Decision<String>(decidedToBlock())
                    .onBlock(() -> "blocked")
                    .onProceed(() -> { called[0] = true; return "proceed"; })
                    .onChallenge(() -> { called[1] = true; return "challenge"; })
                    .onDefer(() -> { called[2] = true; return "deferred"; })
                    .result();

            assertFalse(called[0], "onProceed should not be called");
            assertFalse(called[1], "onChallenge should not be called");
            assertFalse(called[2], "onDefer should not be called");
        }

        @Test
        void non_matching_suppliers_are_not_invoked_on_proceed_verdict() {
            boolean[] called = {false, false, false};

            new Decision<String>(decidedToProceed())
                    .onBlock(() -> { called[0] = true; return "blocked"; })
                    .onProceed(() -> "proceed")
                    .onChallenge(() -> { called[1] = true; return "challenge"; })
                    .onDefer(() -> { called[2] = true; return "deferred"; })
                    .result();

            assertFalse(called[0], "onBlock should not be called");
            assertFalse(called[1], "onChallenge should not be called");
            assertFalse(called[2], "onDefer should not be called");
        }

        @Test
        void non_matching_suppliers_are_not_invoked_on_challenge_verdict() {
            boolean[] called = {false, false, false};

            new Decision<String>(decidedToChallenge())
                    .onBlock(() -> { called[0] = true; return "blocked"; })
                    .onChallenge(() -> "challenge")
                    .onProceed(() -> { called[1] = true; return "proceed"; })
                    .onDefer(() -> { called[2] = true; return "deferred"; })
                    .result();

            assertFalse(called[0], "onBlock should not be called");
            assertFalse(called[1], "onProceed should not be called");
            assertFalse(called[2], "onDefer should not be called");
        }

        @Test
        void non_matching_suppliers_are_not_invoked_on_defer_verdict() {
            boolean[] called = {false, false, false};

            new Decision<String>(decidedToDefer())
                    .onBlock(() -> { called[0] = true; return "blocked"; })
                    .onProceed(() -> { called[1] = true; return "proceed"; })
                    .onChallenge(() -> { called[2] = true; return "challenge"; })
                    .onDefer(() -> "deferred")
                    .result();

            assertFalse(called[0], "onBlock should not be called");
            assertFalse(called[1], "onProceed should not be called");
            assertFalse(called[2], "onChallenge should not be called");
        }
    }

    // =========================================================================

    @Nested
    class EmptyResult {

        @Test
        void result_is_empty_when_no_handler_is_registered() {
            var result = new Decision<String>(decidedToBlock()).result();
            assertEquals(Optional.empty(), result);
        }

        @Test
        void result_is_empty_when_only_non_matching_handlers_are_registered() {
            var result = new Decision<String>(decidedToBlock())
                    .onProceed(() -> "proceed")
                    .onChallenge(() -> "challenge")
                    .onDefer(() -> "deferred")
                    .result();

            assertEquals(Optional.empty(), result);
        }
    }

    // =========================================================================

    @Nested
    class Chaining {

        @Test
        void each_handler_method_returns_the_same_decision_instance() {
            var decision = new Decision<String>(decidedToBlock());

            assertSame(decision, decision.onBlock(() -> "blocked"));
            assertSame(decision, decision.onProceed(() -> "proceed"));
            assertSame(decision, decision.onChallenge(() -> "challenge"));
            assertSame(decision, decision.onDefer(() -> "deferred"));
        }

        @Test
        void result_is_stable_across_multiple_calls() {
            var decision = new Decision<String>(decidedToBlock())
                    .onBlock(() -> "blocked");

            assertEquals(decision.result(), decision.result());
        }
    }
}