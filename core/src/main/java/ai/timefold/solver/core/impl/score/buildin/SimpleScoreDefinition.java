package ai.timefold.solver.core.impl.score.buildin;

import java.util.Arrays;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.config.score.trend.InitializingScoreTrendLevel;
import ai.timefold.solver.core.impl.score.definition.AbstractScoreDefinition;
import ai.timefold.solver.core.impl.score.trend.InitializingScoreTrend;

public class SimpleScoreDefinition extends AbstractScoreDefinition<SimpleScore> {

    public SimpleScoreDefinition() {
        super(new String[] { "score" });
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
        return 1;
    }

    @Override
    public int getFeasibleLevelsSize() {
        return 0;
    }

    @Override
    public Class<SimpleScore> getScoreClass() {
        return SimpleScore.class;
    }

    @Override
    public SimpleScore getZeroScore() {
        return SimpleScore.ZERO;
    }

    @Override
    public SimpleScore getOneSoftestScore() {
        return SimpleScore.ONE;
    }

    @Override
    public SimpleScore parseScore(String scoreString) {
        return SimpleScore.parseScore(scoreString);
    }

    @Override
    public SimpleScore fromLevelNumbers(Number[] levelNumbers) {
        if (levelNumbers.length != getLevelsSize()) {
            throw new IllegalStateException("The levelNumbers (" + Arrays.toString(levelNumbers)
                    + ")'s length (" + levelNumbers.length + ") must equal the levelSize (" + getLevelsSize() + ").");
        }
        return SimpleScore.of((Integer) levelNumbers[0]);
    }

    @Override
    public SimpleScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend, SimpleScore score) {
        var trendLevels = initializingScoreTrend.trendLevels();
        return SimpleScore.of(trendLevels[0] == InitializingScoreTrendLevel.ONLY_DOWN ? score.score() : Integer.MAX_VALUE);
    }

    @Override
    public SimpleScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend, SimpleScore score) {
        var trendLevels = initializingScoreTrend.trendLevels();
        return SimpleScore.of(trendLevels[0] == InitializingScoreTrendLevel.ONLY_UP ? score.score() : Integer.MIN_VALUE);
    }

    @Override
    public SimpleScore divideBySanitizedDivisor(SimpleScore dividend, SimpleScore divisor) {
        var dividendScore = dividend.score();
        var divisorScore = sanitize(divisor.score());
        return fromLevelNumbers(
                new Number[] {
                        divide(dividendScore, divisorScore)
                });
    }

    @Override
    public Class<?> getNumericType() {
        return int.class;
    }
}
