package ai.timefold.solver.core.api.score;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.calculator.ConstraintMatchAwareIncrementalScoreCalculator;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.score.constraint.ConstraintRef;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;
import ai.timefold.solver.core.api.solver.SolutionManager;

import org.jspecify.annotations.NonNull;

/**
 * Build by {@link SolutionManager#explain(Object)} to hold {@link ConstraintMatchTotal}s and {@link Indictment}s
 * necessary to explain the quality of a particular {@link Score}.
 * <p>
 * For a simplified, faster and JSON-friendly alternative, see {@link ScoreAnalysis}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the actual score type
 */
public interface ScoreExplanation<Solution_, Score_ extends Score<Score_>> {

    /**
     * Retrieve the {@link PlanningSolution} that the score being explained comes from.
     */
    @NonNull
    Solution_ getSolution();

    /**
     * Return the {@link Score} being explained.
     * If the specific {@link Score} type used by the {@link PlanningSolution} is required,
     * call {@link #getSolution()} and retrieve it from there.
     */
    @NonNull
    Score_ getScore();

    /**
     * Whether {@link #getSolution()} is initialized or not.
     * 
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * Returns a diagnostic text that explains the solution through the {@link ConstraintMatch} API to identify which
     * constraints or planning entities cause that score quality.
     * <p>
     * In case of an {@link Score#isFeasible() infeasible} solution, this can help diagnose the cause of that.
     *
     * <p>
     * Do not parse the return value, its format may change without warning.
     * Instead, to provide this information in a UI or a service,
     * use {@link ScoreExplanation#getConstraintMatchTotalMap()} and {@link ScoreExplanation#getIndictmentMap()}
     * and convert those into a domain-specific API.
     */
    @NonNull
    String getSummary();

    /**
     * Explains the {@link Score} of {@link #getScore()} by splitting it up per {@link Constraint}.
     * <p>
     * The sum of {@link ConstraintMatchTotal#getScore()} equals {@link #getScore()}.
     *
     * @return the key is the constraintId
     *         (to create one, use {@link ConstraintRef#composeConstraintId(String, String)}).
     * @see #getIndictmentMap()
     */
    @NonNull
    Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap();

    /**
     * Explains the {@link Score} of {@link #getScore()} for all constraints.
     * The return value of this method is determined by several factors:
     *
     * <ul>
     * <li>
     * With Constraint Streams, the user has an option to provide a custom justification mapping,
     * implementing {@link ConstraintJustification}.
     * If provided, every {@link ConstraintMatch} of such constraint will be associated with this custom justification class.
     * Every constraint not associated with a custom justification class
     * will be associated with {@link DefaultConstraintJustification}.
     * </li>
     * <li>
     * With {@link ConstraintMatchAwareIncrementalScoreCalculator},
     * every {@link ConstraintMatch} will be associated with the justification class that the user created it with.
     * </li>
     * </ul>
     *
     * @return all constraint matches
     * @see #getIndictmentMap()
     */
    @NonNull
    List<ConstraintJustification> getJustificationList();

    /**
     * Explains the {@link Score} of {@link #getScore()} for all constraints
     * justified with a given {@link ConstraintJustification} type.
     * Otherwise, as defined by {@link #getJustificationList()}.
     * May be empty, if the score explanation ran with justification support disabled.
     *
     * @return all constraint matches associated with the given justification class
     * @see #getIndictmentMap()
     */
    default <ConstraintJustification_ extends ConstraintJustification> @NonNull List<ConstraintJustification_>
            getJustificationList(@NonNull Class<? extends ConstraintJustification_> constraintJustificationClass) {
        return getJustificationList()
                .stream()
                .filter(constraintJustification -> constraintJustificationClass
                        .isAssignableFrom(constraintJustification.getClass()))
                .map(constraintJustification -> (ConstraintJustification_) constraintJustification)
                .collect(Collectors.toList());
    }

    /**
     * Explains the impact of each planning entity or problem fact on the {@link Score}.
     * An {@link Indictment} is basically the inverse of a {@link ConstraintMatchTotal}:
     * it is a {@link Score} total for any of the {@link ConstraintMatch#getIndictedObjectList() indicted objects}.
     * <p>
     * The sum of {@link ConstraintMatchTotal#getScore()} differs from {@link #getScore()}
     * because each {@link ConstraintMatch#getScore()} is counted
     * for each of the {@link ConstraintMatch#getIndictedObjectList() indicted objects}.
     *
     * @return the key is a {@link ProblemFactCollectionProperty problem fact} or a
     *         {@link PlanningEntity planning entity}
     * @see #getConstraintMatchTotalMap()
     */
    @NonNull
    Map<Object, Indictment<Score_>> getIndictmentMap();

}
