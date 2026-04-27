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
    class RunnableHandlers {

        @Test
        void onBlock_runnable_is_invoked_for_block_verdict() {
            boolean[] ran = {false};
            new Decision<Void>(decidedToBlock())
                    .onBlock(() -> ran[0] = true)
                    .result();
            assertTrue(ran[0]);
        }

        @Test
        void onProceed_runnable_is_invoked_for_proceed_verdict() {
            boolean[] ran = {false};
            new Decision<Void>(decidedToProceed())
                    .onProceed(() -> ran[0] = true)
                    .result();
            assertTrue(ran[0]);
        }

        @Test
        void onChallenge_runnable_is_invoked_for_challenge_verdict() {
            boolean[] ran = {false};
            new Decision<Void>(decidedToChallenge())
                    .onChallenge(() -> ran[0] = true)
                    .result();
            assertTrue(ran[0]);
        }

        @Test
        void onDefer_runnable_is_invoked_for_defer_verdict() {
            boolean[] ran = {false};
            new Decision<Void>(decidedToDefer())
                    .onDefer(() -> ran[0] = true)
                    .result();
            assertTrue(ran[0]);
        }

        @Test
        void runnable_is_not_invoked_when_verdict_does_not_match() {
            boolean[] ran = {false, false, false};

            new Decision<Void>(decidedToBlock())
                    .onBlock(() -> {})
                    .onProceed(() -> ran[0] = true)
                    .onChallenge(() -> ran[1] = true)
                    .onDefer(() -> ran[2] = true)
                    .result();

            assertFalse(ran[0], "onProceed runnable should not run");
            assertFalse(ran[1], "onChallenge runnable should not run");
            assertFalse(ran[2], "onDefer runnable should not run");
        }

        @Test
        void result_is_empty_when_only_runnable_handler_fires() {
            var result = new Decision<String>(decidedToBlock())
                    .onBlock(() -> {})
                    .result();
            assertEquals(Optional.empty(), result);
        }

        @Test
        void runnable_and_supplier_overloads_can_be_mixed_on_different_verdicts() {
            // BLOCK fires the runnable (side effect); result comes from onProceed supplier
            // but since verdict is BLOCK, onProceed supplier does not fire
            boolean[] ran = {false};
            var result = new Decision<String>(decidedToBlock())
                    .onBlock(() -> ran[0] = true)
                    .onProceed(() -> "proceed")
                    .result();

            assertTrue(ran[0]);
            assertEquals(Optional.empty(), result);
        }

        @Test
        void runnable_overload_returns_same_decision_instance() {
            var decision = new Decision<Void>(decidedToBlock());
            assertSame(decision, decision.onBlock(() -> {}));
            assertSame(decision, decision.onProceed(() -> {}));
            assertSame(decision, decision.onChallenge(() -> {}));
            assertSame(decision, decision.onDefer(() -> {}));
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