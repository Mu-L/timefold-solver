package ai.timefold.solver.core.impl.score.definition;

import ai.timefold.solver.core.api.score.IBendableScore;
import ai.timefold.solver.core.api.score.Score;

public abstract class AbstractBendableScoreDefinition<Score_ extends Score<Score_>>
        extends AbstractScoreDefinition<Score_>
        implements ScoreDefinition<Score_> {

    protected static String[] generateLevelLabels(int hardLevelsSize, int softLevelsSize) {
        if (hardLevelsSize < 0 || softLevelsSize < 0) {
            throw new IllegalArgumentException("The hardLevelsSize (" + hardLevelsSize
                    + ") and softLevelsSize (" + softLevelsSize + ") should be positive.");
        }
        var levelLabels = new String[hardLevelsSize + softLevelsSize];
        for (var i = 0; i < levelLabels.length; i++) {
            String labelPrefix;
            if (i < hardLevelsSize) {
                labelPrefix = "hard " + i;
            } else {
                labelPrefix = "soft " + (i - hardLevelsSize);
            }
            levelLabels[i] = labelPrefix + " score";
        }
        return levelLabels;
    }

    protected final int hardLevelsSize;
    protected final int softLevelsSize;

    public AbstractBendableScoreDefinition(int hardLevelsSize, int softLevelsSize) {
        super(generateLevelLabels(hardLevelsSize, softLevelsSize));
        this.hardLevelsSize = hardLevelsSize;
        this.softLevelsSize = softLevelsSize;
    }

    public int getHardLevelsSize() {
        return hardLevelsSize;
    }

    public int getSoftLevelsSize() {
        return softLevelsSize;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
        return hardLevelsSize + softLevelsSize;
    }

    @Override
    public int getFeasibleLevelsSize() {
        return hardLevelsSize;
    }

    @Override
    public boolean isCompatibleArithmeticArgument(Score_ score) {
        if (super.isCompatibleArithmeticArgument(score)) {
            var bendableScore = (IBendableScore<?>) score;
            return getLevelsSize() == bendableScore.levelsSize()
                    && getHardLevelsSize() == bendableScore.hardLevelsSize()
                    && getSoftLevelsSize() == bendableScore.softLevelsSize();
        }
        return false;
    }
}
