package ai.timefold.solver.core.impl.score;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.impl.score.director.InnerScore;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;

import org.jspecify.annotations.NonNull;

public final class DefaultScoreExplanation<Solution_, Score_ extends Score<Score_>>
        implements ScoreExplanation<Solution_, Score_> {

    private static final int DEFAULT_SCORE_EXPLANATION_INDICTMENT_LIMIT = 5;
    private static final int DEFAULT_SCORE_EXPLANATION_CONSTRAINT_MATCH_LIMIT = 2;

    private final Solution_ solution;
    private final InnerScore<Score_> innerScore;
    private final Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap;
    private final List<ConstraintJustification> constraintJustificationList;
    private final Map<Object, Indictment<Score_>> indictmentMap;
    private final AtomicReference<String> summary = new AtomicReference<>(); // Will be calculated lazily.

    public static <Score_ extends Score<Score_>> String explainScore(InnerScore<Score_> workingScore,
            Collection<ConstraintMatchTotal<Score_>> constraintMatchTotalCollection,
            Collection<Indictment<Score_>> indictmentCollection) {
        return explainScore(workingScore, constraintMatchTotalCollection, indictmentCollection,
                DEFAULT_SCORE_EXPLANATION_INDICTMENT_LIMIT, DEFAULT_SCORE_EXPLANATION_CONSTRAINT_MATCH_LIMIT);
    }

    public static <Score_ extends Score<Score_>> String explainScore(InnerScore<Score_> workingScore,
            Collection<ConstraintMatchTotal<Score_>> constraintMatchTotalCollection,
            Collection<Indictment<Score_>> indictmentCollection, int indictmentLimit, int constraintMatchLimit) {
        var scoreExplanation = new StringBuilder((constraintMatchTotalCollection.size() + 4 + 2 * indictmentLimit) * 80);
        scoreExplanation.append("""
                Explanation of score (%s)%s:
                    Constraint matches:
                """.formatted(workingScore, workingScore.isFullyAssigned() ? "" : " for an uninitialized solution"));

        Comparator<ConstraintMatchTotal<Score_>> constraintMatchTotalComparator = comparing(ConstraintMatchTotal::getScore);
        Comparator<ConstraintMatch<Score_>> constraintMatchComparator = comparing(ConstraintMatch::getScore);
        constraintMatchTotalCollection.stream()
                .sorted(constraintMatchTotalComparator)
                .forEach(constraintMatchTotal -> {
                    var constraintMatchSet = constraintMatchTotal.getConstraintMatchSet();
                    scoreExplanation.append("""
                                    %s: constraint (%s) has %s matches:
                            """.formatted(constraintMatchTotal.getScore().toShortString(),
                            constraintMatchTotal.getConstraintRef().constraintName(), constraintMatchSet.size()));
                    constraintMatchSet.stream()
                            .sorted(constraintMatchComparator)
                            .limit(constraintMatchLimit)
                            .forEach(constraintMatch -> {
                                if (constraintMatch.getJustification() == null) {
                                    scoreExplanation.append("""
                                                       %s: unjustified
                                            """.formatted(constraintMatch.getScore().toShortString()));
                                } else {
                                    scoreExplanation.append("""
                                                        %s: justified with (%s)
                                            """.formatted(constraintMatch.getScore().toShortString(),
                                            constraintMatch.getJustification()));
                                }
                            });
                    if (constraintMatchSet.size() > constraintMatchLimit) {
                        scoreExplanation.append("""
                                            ...
                                """);
                    }
                });

        var indictmentCount = indictmentCollection.size();
        if (indictmentLimit < indictmentCount) {
            scoreExplanation.append("""
                        Indictments (top %s of %s):
                    """.formatted(indictmentLimit, indictmentCount));
        } else {
            scoreExplanation.append("""
                        Indictments:
                    """);
        }

        Comparator<Indictment<Score_>> indictmentComparator = comparing(Indictment::getScore);
        Comparator<ConstraintMatch<Score_>> constraintMatchScoreComparator = comparing(ConstraintMatch::getScore);
        indictmentCollection.stream()
                .sorted(indictmentComparator)
                .limit(indictmentLimit)
                .forEach(indictment -> {
                    var constraintMatchSet = indictment.getConstraintMatchSet();
                    scoreExplanation.append("""
                                    %s: indicted with (%s) has %s matches:
                            """.formatted(indictment.getScore().toShortString(), indictment.getIndictedObject(),
                            constraintMatchSet.size()));
                    constraintMatchSet.stream()
                            .sorted(constraintMatchScoreComparator)
                            .limit(constraintMatchLimit)
                            .forEach(constraintMatch -> scoreExplanation.append("""
                                                %s: constraint (%s)
                                    """.formatted(constraintMatch.getScore().toShortString(),
                                    constraintMatch.getConstraintRef().constraintName())));
                    if (constraintMatchSet.size() > constraintMatchLimit) {
                        scoreExplanation.append("""
                                            ...
                                """);
                    }
                });
        if (indictmentCount > indictmentLimit) {
            scoreExplanation.append("""
                            ...
                    """);
        }
        return scoreExplanation.toString();
    }

    public DefaultScoreExplanation(InnerScoreDirector<Solution_, Score_> scoreDirector) {
        this(scoreDirector.getWorkingSolution(), scoreDirector.calculateScore(), scoreDirector.getConstraintMatchTotalMap(),
                scoreDirector.getIndictmentMap());
    }

    public DefaultScoreExplanation(Solution_ solution, InnerScore<Score_> innerScore,
            Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap,
            Map<Object, Indictment<Score_>> indictmentMap) {
        this.solution = solution;
        this.innerScore = innerScore;
        this.constraintMatchTotalMap = constraintMatchTotalMap;
        List<ConstraintJustification> workingConstraintJustificationList = new ArrayList<>();
        for (ConstraintMatchTotal<Score_> constraintMatchTotal : constraintMatchTotalMap.values()) {
            for (ConstraintMatch<Score_> constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                ConstraintJustification justification = constraintMatch.getJustification();
                if (justification != null) {
                    workingConstraintJustificationList.add(justification);
                }
            }
        }
        this.constraintJustificationList =
                workingConstraintJustificationList.isEmpty() ? Collections.emptyList() : workingConstraintJustificationList;
        this.indictmentMap = indictmentMap;
    }

    @Override
    public @NonNull Solution_ getSolution() {
        return solution;
    }

    @Override
    public @NonNull Score_ getScore() {
        return innerScore.raw();
    }

    @Override
    public boolean isInitialized() {
        return innerScore.isFullyAssigned();
    }

    @Override
    public @NonNull Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        return constraintMatchTotalMap;
    }

    @Override
    public @NonNull List<ConstraintJustification> getJustificationList() {
        return constraintJustificationList;
    }

    @Override
    public @NonNull Map<Object, Indictment<Score_>> getIndictmentMap() {
        return indictmentMap;
    }

    @Override
    public @NonNull String getSummary() {
        return summary.updateAndGet(currentSummary -> Objects.requireNonNullElseGet(currentSummary,
                () -> explainScore(innerScore, constraintMatchTotalMap.values(), indictmentMap.values())));
    }

    @Override
    public String toString() {
        return getSummary(); // So that this class can be used in strings directly.
    }
}
