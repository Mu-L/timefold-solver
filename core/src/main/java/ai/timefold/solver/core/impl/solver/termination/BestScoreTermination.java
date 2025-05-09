package ai.timefold.solver.core.impl.solver.termination;

import java.util.Arrays;
import java.util.Objects;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.score.definition.ScoreDefinition;
import ai.timefold.solver.core.impl.score.director.InnerScore;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;

import org.jspecify.annotations.NullMarked;

@NullMarked
final class BestScoreTermination<Solution_>
        extends AbstractUniversalTermination<Solution_> {

    private final int levelsSize;
    private final Score<?> bestScoreLimit;
    private final double[] timeGradientWeightNumbers;

    public BestScoreTermination(ScoreDefinition<?> scoreDefinition, Score<?> bestScoreLimit,
            double[] timeGradientWeightNumbers) {
        levelsSize = scoreDefinition.getLevelsSize();
        this.bestScoreLimit = Objects.requireNonNull(bestScoreLimit, "The bestScoreLimit cannot be null.");
        this.timeGradientWeightNumbers = timeGradientWeightNumbers;
        if (timeGradientWeightNumbers.length != levelsSize - 1) {
            throw new IllegalStateException(
                    "The timeGradientWeightNumbers (%s)'s length (%d) is not 1 less than the levelsSize (%d)."
                            .formatted(Arrays.toString(timeGradientWeightNumbers), timeGradientWeightNumbers.length,
                                    scoreDefinition.getLevelsSize()));
        }
    }

    @Override
    public boolean isSolverTerminated(SolverScope<Solution_> solverScope) {
        return isTerminated(solverScope.isBestSolutionInitialized(), solverScope.getBestScore().raw());
    }

    @Override
    public boolean isPhaseTerminated(AbstractPhaseScope<Solution_> phaseScope) {
        return isTerminated(phaseScope.isBestSolutionInitialized(), phaseScope.getBestScore().raw());
    }

    private <Score_ extends Score<Score_>> boolean isTerminated(boolean bestSolutionInitialized, Score_ bestScore) {
        return bestSolutionInitialized && bestScore.compareTo(getBestScoreLimit()) >= 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public double calculateSolverTimeGradient(SolverScope<Solution_> solverScope) {
        var startingInitializedScore = solverScope.getStartingInitializedScore();
        var bestScore = solverScope.getBestScore();
        return calculateTimeGradient((Score) startingInitializedScore, getBestScoreLimit(), (Score) bestScore.raw());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public double calculatePhaseTimeGradient(AbstractPhaseScope<Solution_> phaseScope) {
        var startingInitializedScore = phaseScope.<Score> getStartingScore();
        var bestScore = phaseScope.<Score> getBestScore();
        return calculateTimeGradient(startingInitializedScore.raw(), getBestScoreLimit(), bestScore.raw());
    }

    /**
     * CH is not allowed to compute a time gradient.
     * Therefore the scores at this point no longer need to be {@link InnerScore}.
     */
    <Score_ extends Score<Score_>> double calculateTimeGradient(Score_ startScore, Score_ endScore, Score_ score) {
        var totalDiff = endScore.subtract(startScore);
        var totalDiffNumbers = totalDiff.toLevelNumbers();
        var scoreDiff = score.subtract(startScore);
        var scoreDiffNumbers = scoreDiff.toLevelNumbers();
        if (scoreDiffNumbers.length != totalDiffNumbers.length) {
            throw new IllegalStateException("The startScore (" + startScore + "), endScore (" + endScore
                    + ") and score (" + score + ") don't have the same levelsSize.");
        }
        return calculateTimeGradient(totalDiffNumbers, scoreDiffNumbers, timeGradientWeightNumbers,
                levelsSize);
    }

    /**
     *
     * @param totalDiffNumbers never null
     * @param scoreDiffNumbers never null
     * @param timeGradientWeightNumbers never null
     * @param levelDepth The number of levels of the diffNumbers that are included
     * @return {@code 0.0 <= value <= 1.0}
     */
    static double calculateTimeGradient(Number[] totalDiffNumbers, Number[] scoreDiffNumbers,
            double[] timeGradientWeightNumbers, int levelDepth) {
        var timeGradient = 0.0;
        var remainingTimeGradient = 1.0;
        for (var i = 0; i < levelDepth; i++) {
            double levelTimeGradientWeight;
            if (i != (levelDepth - 1)) {
                levelTimeGradientWeight = remainingTimeGradient * timeGradientWeightNumbers[i];
                remainingTimeGradient -= levelTimeGradientWeight;
            } else {
                levelTimeGradientWeight = remainingTimeGradient;
                remainingTimeGradient = 0.0;
            }
            var totalDiffLevel = totalDiffNumbers[i].doubleValue();
            var scoreDiffLevel = scoreDiffNumbers[i].doubleValue();
            if (scoreDiffLevel == totalDiffLevel) {
                // Max out this level
                timeGradient += levelTimeGradientWeight;
            } else if (scoreDiffLevel > totalDiffLevel) {
                // Max out this level and all softer levels too
                timeGradient += levelTimeGradientWeight + remainingTimeGradient;
                break;
            } else if (scoreDiffLevel == 0.0) {
                // Ignore this level
                // timeGradient += 0.0
            } else if (scoreDiffLevel < 0.0) {
                // Ignore this level and all softer levels too
                // timeGradient += 0.0
                break;
            } else {
                var levelTimeGradient = scoreDiffLevel / totalDiffLevel;
                timeGradient += levelTimeGradient * levelTimeGradientWeight;
            }

        }
        if (timeGradient > 1.0) {
            // Rounding error due to calculating with doubles
            timeGradient = 1.0;
        }
        return timeGradient;
    }

    @SuppressWarnings("unchecked")
    public <Score_ extends Score<Score_>> Score_ getBestScoreLimit() {
        return (Score_) bestScoreLimit;
    }

    @Override
    public String toString() {
        return "BestScore(" + bestScoreLimit + ")";
    }

}
