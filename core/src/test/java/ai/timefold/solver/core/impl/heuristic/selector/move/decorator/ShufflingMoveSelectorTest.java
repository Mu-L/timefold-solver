package ai.timefold.solver.core.impl.heuristic.selector.move.decorator;

import static ai.timefold.solver.core.testutil.PlannerAssert.assertAllCodesOfMoveSelector;
import static ai.timefold.solver.core.testutil.PlannerAssert.verifyPhaseLifecycle;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.impl.heuristic.move.DummyMove;
import ai.timefold.solver.core.impl.heuristic.selector.SelectorTestUtils;
import ai.timefold.solver.core.impl.heuristic.selector.move.MoveSelector;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.phase.scope.AbstractStepScope;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.testutil.PlannerTestUtils;
import ai.timefold.solver.core.testutil.TestRandom;

import org.junit.jupiter.api.Test;

class ShufflingMoveSelectorTest {

    @Test
    void cacheTypeSolver() {
        run(SelectionCacheType.SOLVER, 1);
    }

    @Test
    void cacheTypePhase() {
        run(SelectionCacheType.PHASE, 2);
    }

    @Test
    void cacheTypeStep() {
        run(SelectionCacheType.STEP, 3);
    }

    public void run(SelectionCacheType cacheType, int timesCalled) {
        MoveSelector childMoveSelector = SelectorTestUtils.mockMoveSelector(
                new DummyMove("a1"), new DummyMove("a2"), new DummyMove("a3"));

        ShufflingMoveSelector moveSelector = new ShufflingMoveSelector(childMoveSelector, cacheType);
        verify(childMoveSelector, times(1)).isNeverEnding();

        TestRandom workingRandom = new TestRandom(2, 0);
        SolverScope solverScope = mock(SolverScope.class);
        when(solverScope.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.solvingStarted(solverScope);

        AbstractPhaseScope phaseScopeA = PlannerTestUtils.delegatingPhaseScope(solverScope);
        moveSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = PlannerTestUtils.delegatingStepScope(phaseScopeA);
        moveSelector.stepStarted(stepScopeA1);
        assertAllCodesOfMoveSelector(moveSelector, "a2", "a1", "a3");
        moveSelector.stepEnded(stepScopeA1);

        workingRandom.reset(0, 1);
        AbstractStepScope stepScopeA2 = PlannerTestUtils.delegatingStepScope(phaseScopeA);
        moveSelector.stepStarted(stepScopeA2);
        if (cacheType.compareTo(SelectionCacheType.STEP) > 0) {
            // From a1, a2, a3
            assertAllCodesOfMoveSelector(moveSelector, "a3", "a1", "a2");
        } else {
            // Reset from a1, a2, a3
            assertAllCodesOfMoveSelector(moveSelector, "a3", "a2", "a1");
        }
        moveSelector.stepEnded(stepScopeA2);

        moveSelector.phaseEnded(phaseScopeA);

        AbstractPhaseScope phaseScopeB = PlannerTestUtils.delegatingPhaseScope(solverScope);
        moveSelector.phaseStarted(phaseScopeB);

        workingRandom.reset(1, 0);
        AbstractStepScope stepScopeB1 = PlannerTestUtils.delegatingStepScope(phaseScopeB);
        moveSelector.stepStarted(stepScopeB1);
        if (cacheType.compareTo(SelectionCacheType.PHASE) > 0) {
            // From a3, a1, a2
            assertAllCodesOfMoveSelector(moveSelector, "a2", "a3", "a1");
        } else {
            // Reset from a1, a2, a3
            assertAllCodesOfMoveSelector(moveSelector, "a3", "a1", "a2");
        }
        moveSelector.stepEnded(stepScopeB1);

        moveSelector.phaseEnded(phaseScopeB);

        moveSelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childMoveSelector, 1, 2, 3);
        verify(childMoveSelector, times(timesCalled)).iterator();
        verify(childMoveSelector, times(timesCalled)).getSize();
    }

}
