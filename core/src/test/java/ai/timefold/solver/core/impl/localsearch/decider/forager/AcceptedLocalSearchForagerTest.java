package ai.timefold.solver.core.impl.localsearch.decider.forager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Random;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.config.localsearch.decider.forager.LocalSearchPickEarlyType;
import ai.timefold.solver.core.config.solver.monitoring.SolverMetric;
import ai.timefold.solver.core.impl.heuristic.move.DummyMove;
import ai.timefold.solver.core.impl.heuristic.move.LegacyMoveAdapter;
import ai.timefold.solver.core.impl.localsearch.decider.forager.finalist.HighestScoreFinalistPodium;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchMoveScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchPhaseScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.score.buildin.SimpleScoreDefinition;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.testdomain.TestdataSolution;
import ai.timefold.solver.core.testutil.TestRandom;

import org.junit.jupiter.api.Test;

class AcceptedLocalSearchForagerTest {

    @Test
    void pickMoveMaxScoreAccepted() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.NEVER, Integer.MAX_VALUE, true);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-20), true);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-1), false);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-20), false);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-2), true);
        LocalSearchMoveScope<TestdataSolution> e = createMoveScope(stepScope, SimpleScore.of(-300), true);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(e);
        assertThat(forager.isQuitEarly()).isFalse();
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        // Post conditions
        assertThat(pickedScope).isSameAs(d);
        forager.phaseEnded(phaseScope);
    }

    @Test
    void pickMoveMaxScoreUnaccepted() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.NEVER, Integer.MAX_VALUE, true);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-20), false);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-1), false);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-20), false);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-2), false);
        LocalSearchMoveScope<TestdataSolution> e = createMoveScope(stepScope, SimpleScore.of(-300), false);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(e);
        assertThat(forager.isQuitEarly()).isFalse();
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        // Post conditions
        assertThat(pickedScope).isSameAs(b);
        forager.phaseEnded(phaseScope);
    }

    @Test
    void pickMoveFirstBestScoreImproving() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.FIRST_BEST_SCORE_IMPROVING, Integer.MAX_VALUE, true);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-1), false);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-20), true);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-300), true);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-1), true);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isTrue();
        // Post conditions
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        assertThat(pickedScope).isSameAs(d);
        forager.phaseEnded(phaseScope);
    }

    @Test
    void pickMoveFirstLastStepScoreImproving() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.FIRST_LAST_STEP_SCORE_IMPROVING, Integer.MAX_VALUE, true);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-1), false);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-300), true);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-4000), true);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-20), true);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isTrue();
        // Post conditions
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        assertThat(pickedScope).isSameAs(d);
        forager.phaseEnded(phaseScope);
    }

    @Test
    void pickMoveAcceptedBreakTieRandomly() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.NEVER, 4, true);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-20), false);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-1), true);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-1), true);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-20), true);
        LocalSearchMoveScope<TestdataSolution> e = createMoveScope(stepScope, SimpleScore.of(-1), true);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(e);
        assertThat(forager.isQuitEarly()).isTrue();
        // Post conditions
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        assertThat(pickedScope).isSameAs(c);
        forager.phaseEnded(phaseScope);
    }

    @Test
    void pickMoveAcceptedBreakTieFirst() {
        // Setup
        LocalSearchForager<TestdataSolution> forager = new AcceptedLocalSearchForager<>(new HighestScoreFinalistPodium<>(),
                LocalSearchPickEarlyType.NEVER, 4, false);
        LocalSearchPhaseScope<TestdataSolution> phaseScope = createPhaseScope();
        forager.phaseStarted(phaseScope);
        LocalSearchStepScope<TestdataSolution> stepScope = new LocalSearchStepScope<>(phaseScope);
        forager.stepStarted(stepScope);
        // Pre conditions
        LocalSearchMoveScope<TestdataSolution> a = createMoveScope(stepScope, SimpleScore.of(-20), false);
        LocalSearchMoveScope<TestdataSolution> b = createMoveScope(stepScope, SimpleScore.of(-1), true);
        LocalSearchMoveScope<TestdataSolution> c = createMoveScope(stepScope, SimpleScore.of(-1), true);
        LocalSearchMoveScope<TestdataSolution> d = createMoveScope(stepScope, SimpleScore.of(-20), true);
        LocalSearchMoveScope<TestdataSolution> e = createMoveScope(stepScope, SimpleScore.of(-1), true);
        // Do stuff
        forager.addMove(a);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(b);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(c);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(d);
        assertThat(forager.isQuitEarly()).isFalse();
        forager.addMove(e);
        assertThat(forager.isQuitEarly()).isTrue();
        // Post conditions
        LocalSearchMoveScope<TestdataSolution> pickedScope = forager.pickMove(stepScope);
        assertThat(pickedScope).isSameAs(b);
        forager.phaseEnded(phaseScope);
    }

    private static LocalSearchPhaseScope<TestdataSolution> createPhaseScope() {
        SolverScope<TestdataSolution> solverScope = new SolverScope<>();
        LocalSearchPhaseScope<TestdataSolution> phaseScope = new LocalSearchPhaseScope<>(solverScope, 0);
        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        when(scoreDirector.getSolutionDescriptor()).thenReturn(TestdataSolution.buildSolutionDescriptor());
        when(scoreDirector.getScoreDefinition()).thenReturn(new SimpleScoreDefinition());
        solverScope.setScoreDirector(scoreDirector);
        Random workingRandom = new TestRandom(1, 1);
        solverScope.setWorkingRandom(workingRandom);
        solverScope.setInitializedBestScore(SimpleScore.of(-10));
        solverScope.setSolverMetricSet(EnumSet.of(SolverMetric.MOVE_EVALUATION_COUNT));
        LocalSearchStepScope<TestdataSolution> lastLocalSearchStepScope = new LocalSearchStepScope<>(phaseScope);
        lastLocalSearchStepScope.setInitializedScore(SimpleScore.of(-100));
        phaseScope.setLastCompletedStepScope(lastLocalSearchStepScope);
        return phaseScope;
    }

    private static LocalSearchMoveScope<TestdataSolution> createMoveScope(LocalSearchStepScope<TestdataSolution> stepScope,
            SimpleScore score, boolean accepted) {
        LocalSearchMoveScope<TestdataSolution> moveScope =
                new LocalSearchMoveScope<>(stepScope, 0, new LegacyMoveAdapter<>(new DummyMove()));
        moveScope.setInitializedScore(score);
        moveScope.setAccepted(accepted);
        return moveScope;
    }

}
