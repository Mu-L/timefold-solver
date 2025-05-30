package ai.timefold.solver.core.impl.localsearch.decider.acceptor.stepcountinghillclimbing;

import ai.timefold.solver.core.config.localsearch.decider.acceptor.stepcountinghillclimbing.StepCountingHillClimbingType;
import ai.timefold.solver.core.impl.localsearch.decider.acceptor.AbstractAcceptor;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchMoveScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchPhaseScope;
import ai.timefold.solver.core.impl.localsearch.scope.LocalSearchStepScope;
import ai.timefold.solver.core.impl.score.director.InnerScore;

public class StepCountingHillClimbingAcceptor<Solution_> extends AbstractAcceptor<Solution_> {

    protected int stepCountingHillClimbingSize = -1;
    protected StepCountingHillClimbingType stepCountingHillClimbingType;

    protected InnerScore<?> thresholdScore;
    protected int count = -1;

    public StepCountingHillClimbingAcceptor(int stepCountingHillClimbingSize,
            StepCountingHillClimbingType stepCountingHillClimbingType) {
        this.stepCountingHillClimbingSize = stepCountingHillClimbingSize;
        this.stepCountingHillClimbingType = stepCountingHillClimbingType;
        if (stepCountingHillClimbingSize <= 0) {
            throw new IllegalArgumentException("The stepCountingHillClimbingSize (" + stepCountingHillClimbingSize
                    + ") cannot be negative or zero.");
        }
        if (stepCountingHillClimbingType == null) {
            throw new IllegalArgumentException("The stepCountingHillClimbingType (" + stepCountingHillClimbingType
                    + ") cannot be null.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        thresholdScore = phaseScope.getBestScore();
        count = 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean isAccepted(LocalSearchMoveScope<Solution_> moveScope) {
        InnerScore lastStepScore = moveScope.getStepScope().getPhaseScope().getLastCompletedStepScope().getScore();
        InnerScore moveScore = moveScope.getScore();
        if (moveScore.compareTo(lastStepScore) >= 0) {
            return true;
        }
        return moveScore.compareTo(thresholdScore) >= 0;
    }

    @Override
    public void stepEnded(LocalSearchStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        count += determineCountIncrement(stepScope);
        if (count >= stepCountingHillClimbingSize) {
            thresholdScore = stepScope.getScore();
            count = 0;
        }
    }

    private int determineCountIncrement(LocalSearchStepScope<Solution_> stepScope) {
        return switch (stepCountingHillClimbingType) {
            case SELECTED_MOVE -> {
                long selectedMoveCount = stepScope.getSelectedMoveCount();
                yield selectedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) selectedMoveCount;
            }
            case ACCEPTED_MOVE -> {
                long acceptedMoveCount = stepScope.getAcceptedMoveCount();
                yield acceptedMoveCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) acceptedMoveCount;
            }
            case STEP -> 1;
            case EQUAL_OR_IMPROVING_STEP ->
                stepScope.getScore().compareTo(stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) >= 0 ? 1 : 0;
            case IMPROVING_STEP ->
                stepScope.getScore().compareTo(stepScope.getPhaseScope().getLastCompletedStepScope().getScore()) > 0 ? 1 : 0;
        };
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        thresholdScore = null;
        count = -1;
    }

}
