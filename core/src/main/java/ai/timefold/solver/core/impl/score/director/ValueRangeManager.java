package ai.timefold.solver.core.impl.score.director;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ai.timefold.solver.core.api.domain.valuerange.CountableValueRange;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.api.solver.ProblemSizeStatistics;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.solution.descriptor.ProblemScaleTracker;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.domain.valuerange.buildin.EmptyValueRange;
import ai.timefold.solver.core.impl.domain.valuerange.buildin.bigdecimal.BigDecimalValueRange;
import ai.timefold.solver.core.impl.domain.valuerange.buildin.composite.NullAllowingCountableValueRange;
import ai.timefold.solver.core.impl.domain.valuerange.buildin.primdouble.DoubleValueRange;
import ai.timefold.solver.core.impl.domain.valuerange.descriptor.ValueRangeDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.BasicVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.util.MathUtils;
import ai.timefold.solver.core.impl.util.MutableInt;
import ai.timefold.solver.core.impl.util.MutableLong;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Caches value ranges for the current working solution,
 * allowing to quickly access these cached value ranges when needed.
 * 
 * <p>
 * Outside a {@link ProblemChange}, value ranges are not allowed to change.
 * Call {@link #reset(Object)} every time the working solution changes through a problem fact,
 * so that all caches can be invalidated.
 * 
 * <p>
 * Two score directors can never share the same instance of this class;
 * this class contains state that is specific to a particular instance of a working solution.
 * Even a clone of that same solution must not share the same instance of this class,
 * unless {@link #reset(Object)} is called with the clone;
 * failing to follow this rule will result in score corruptions as the cached value ranges reference
 * objects from the original working solution pre-clone.
 *
 * @see CountableValueRange
 * @see ValueRangeProvider
 */
@NullMarked
public final class ValueRangeManager<Solution_> {

    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final Map<ValueRangeDescriptor<Solution_>, CountableValueRange<?>> fromSolutionMap = new IdentityHashMap<>();
    private final Map<Object, Map<ValueRangeDescriptor<Solution_>, CountableValueRange<?>>> fromEntityMap =
            new IdentityHashMap<>();

    private @Nullable Solution_ cachedWorkingSolution = null;
    private @Nullable SolutionInitializationStatistics cachedInitializationStatistics = null;
    private @Nullable ProblemSizeStatistics cachedProblemSizeStatistics = null;

    public static <Solution_> ValueRangeManager<Solution_> of(SolutionDescriptor<Solution_> solutionDescriptor,
            Solution_ solution) {
        var valueRangeManager = new ValueRangeManager<>(solutionDescriptor);
        valueRangeManager.reset(solution);
        return valueRangeManager;
    }

    /**
     * It is not recommended for code other than {@link ScoreDirector} to create instances of this class.
     * See class-level documentation for more details.
     * For safety, prefer using {@link #of(SolutionDescriptor, Object)} to create an instance of this class
     * with a solution already set.
     */
    public ValueRangeManager(SolutionDescriptor<Solution_> solutionDescriptor) {
        this.solutionDescriptor = Objects.requireNonNull(solutionDescriptor);
    }

    public SolutionInitializationStatistics getInitializationStatistics() {
        if (cachedWorkingSolution == null) {
            throw new IllegalStateException(
                    "Impossible state: initialization statistics requested before the working solution is known.");
        }
        return getInitializationStatistics(null);
    }

    public SolutionInitializationStatistics getInitializationStatistics(@Nullable Consumer<Object> finisher) {
        if (cachedWorkingSolution == null) {
            throw new IllegalStateException(
                    "Impossible state: initialization statistics requested before the working solution is known.");
        }
        if (finisher == null) {
            if (cachedInitializationStatistics == null) {
                cachedInitializationStatistics = computeInitializationStatistics(cachedWorkingSolution, null);
            }
        } else {
            // If a finisher is provided, we always recompute the statistics,
            // because the finisher is expected to be called for every entity.
            cachedInitializationStatistics = computeInitializationStatistics(cachedWorkingSolution, finisher);
        }
        return cachedInitializationStatistics;
    }

    public SolutionInitializationStatistics computeInitializationStatistics(Solution_ solution,
            @Nullable Consumer<Object> finisher) {
        /*
         * The score director requires all of these data points,
         * so we calculate them all in a single pass over the entities.
         * This is an important performance improvement,
         * as there are potentially thousands of entities.
         */
        var uninitializedEntityCount = new MutableInt();
        var uninitializedVariableCount = new MutableInt();
        var unassignedValueCount = new MutableInt();
        var notInAnyListValueCount = new MutableInt();
        var genuineEntityCount = new MutableInt();
        var shadowEntityCount = new MutableInt();

        var listVariableDescriptor = solutionDescriptor.getListVariableDescriptor();
        if (listVariableDescriptor != null) {
            var countOnSolution = (int) countOnSolution(listVariableDescriptor.getValueRangeDescriptor(), solution);
            notInAnyListValueCount.add(countOnSolution);
            if (!listVariableDescriptor.allowsUnassignedValues()) {
                // We count every possibly unassigned element in every list variable.
                // And later we subtract the assigned elements.
                unassignedValueCount.add(countOnSolution);
            }
        }

        solutionDescriptor.visitAllEntities(solution, entity -> {
            var entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
            if (entityDescriptor.isGenuine()) {
                genuineEntityCount.increment();
                var uninitializedVariableCountForEntity = entityDescriptor.countUninitializedVariables(entity);
                if (uninitializedVariableCountForEntity > 0) {
                    uninitializedEntityCount.increment();
                    uninitializedVariableCount.add(uninitializedVariableCountForEntity);
                }
            } else {
                shadowEntityCount.increment();
            }
            if (finisher != null) {
                finisher.accept(entity);
            }
            if (!entityDescriptor.hasAnyGenuineListVariables()) {
                return;
            }
            var listVariableEntityDescriptor = listVariableDescriptor.getEntityDescriptor();
            var countOnEntity = listVariableDescriptor.getListSize(entity);
            notInAnyListValueCount.subtract(countOnEntity);
            if (!listVariableDescriptor.allowsUnassignedValues() && listVariableEntityDescriptor.matchesEntity(entity)) {
                unassignedValueCount.subtract(countOnEntity);
            }
            // TODO maybe detect duplicates and elements that are outside the value range
        });
        return new SolutionInitializationStatistics(genuineEntityCount.intValue(),
                shadowEntityCount.intValue(),
                uninitializedEntityCount.intValue(), uninitializedVariableCount.intValue(), unassignedValueCount.intValue(),
                notInAnyListValueCount.intValue());
    }

    public ProblemSizeStatistics getProblemSizeStatistics() {
        if (cachedWorkingSolution == null) {
            throw new IllegalStateException("Impossible state: problem size requested before the working solution is known.");
        } else if (cachedProblemSizeStatistics == null) {
            cachedProblemSizeStatistics = new ProblemSizeStatistics(
                    solutionDescriptor.getGenuineEntityCount(cachedWorkingSolution),
                    solutionDescriptor.getGenuineVariableCount(cachedWorkingSolution),
                    getApproximateValueCount(),
                    getProblemScale());
        }
        return cachedProblemSizeStatistics;
    }

    long getApproximateValueCount() {
        var genuineVariableDescriptorSet =
                Collections.newSetFromMap(new IdentityHashMap<GenuineVariableDescriptor<Solution_>, Boolean>());
        solutionDescriptor.visitAllEntities(cachedWorkingSolution, entity -> {
            var entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
            if (entityDescriptor.isGenuine()) {
                genuineVariableDescriptorSet.addAll(entityDescriptor.getGenuineVariableDescriptorList());
            }
        });
        var out = new MutableLong();
        for (var variableDescriptor : genuineVariableDescriptorSet) {
            var valueRangeDescriptor = variableDescriptor.getValueRangeDescriptor();
            if (valueRangeDescriptor.canExtractValueRangeFromSolution()) {
                out.add(countOnSolution(valueRangeDescriptor, cachedWorkingSolution));
            } else {
                solutionDescriptor.visitEntitiesByEntityClass(cachedWorkingSolution,
                        variableDescriptor.getEntityDescriptor().getEntityClass(),
                        entity -> {
                            out.add(countOnEntity(valueRangeDescriptor, entity));
                            return false;
                        });
            }
        }
        return out.longValue();
    }

    /**
     * Calculates an indication of the size of the problem instance.
     * This is approximately the base 10 logarithm of the search space size.
     *
     * <p>
     * The method uses a logarithmic scale to estimate the problem size,
     * where the base of the logarithm is determined by the maximum value range size.
     * It accounts for both basic variables and list variables in the solution,
     * considering pinned values and value ranges on both entity and solution.
     *
     * @return A non-negative double value representing the approximate base 10 logarithm of the search space size.
     *         Returns {@code 0} if the calculation results in NaN or infinity.
     */
    double getProblemScale() {
        var logBase = Math.max(2, getMaximumValueRangeSize());
        var problemScaleTracker = new ProblemScaleTracker(logBase);
        solutionDescriptor.visitAllEntities(cachedWorkingSolution, entity -> {
            var entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
            if (entityDescriptor.isGenuine()) {
                processProblemScale(entityDescriptor, entity, problemScaleTracker);
            }
        });
        var result = problemScaleTracker.getBasicProblemScaleLog();
        if (problemScaleTracker.getListTotalEntityCount() != 0L) {
            // List variables do not support from entity value ranges
            var totalListValueCount = problemScaleTracker.getListTotalValueCount();
            var totalListMovableValueCount = totalListValueCount - problemScaleTracker.getListPinnedValueCount();
            var possibleTargetsForListValue = problemScaleTracker.getListMovableEntityCount();
            var listVariableDescriptor = solutionDescriptor.getListVariableDescriptor();
            if (listVariableDescriptor != null && listVariableDescriptor.allowsUnassignedValues()) {
                // Treat unassigned values as assigned to a single virtual vehicle for the sake of this calculation
                possibleTargetsForListValue++;
            }

            result += MathUtils.getPossibleArrangementsScaledApproximateLog(MathUtils.LOG_PRECISION, logBase,
                    totalListMovableValueCount, possibleTargetsForListValue);
        }
        var scale = (result / (double) MathUtils.LOG_PRECISION) / MathUtils.getLogInBase(logBase, 10d);
        if (Double.isNaN(scale) || Double.isInfinite(scale)) {
            return 0;
        }
        return scale;
    }

    /**
     * Calculates the maximum value range size across all entities in the working solution.
     * <p>
     * The "maximum value range size" is defined as the largest number of possible values
     * for any genuine variable across all entities.
     * This is determined by inspecting the value range descriptors of each variable in each entity.
     *
     * @return The maximum value range size, or 0 if no genuine variables are found.
     */
    long getMaximumValueRangeSize() {
        return solutionDescriptor.extractAllEntitiesStream(cachedWorkingSolution)
                .mapToLong(entity -> {
                    var entityDescriptor = solutionDescriptor.findEntityDescriptorOrFail(entity.getClass());
                    return entityDescriptor.isGenuine()
                            ? getMaximumValueCount(entityDescriptor, entity)
                            : 0L;
                })
                .max()
                .orElse(0L);
    }

    private long getMaximumValueCount(EntityDescriptor<Solution_> entityDescriptor, Object entity) {
        var maximumValueCount = 0L;
        for (var variableDescriptor : entityDescriptor.getGenuineVariableDescriptorList()) {
            if (variableDescriptor.canExtractValueRangeFromSolution()) {
                maximumValueCount = Math.max(maximumValueCount,
                        countOnSolution(variableDescriptor.getValueRangeDescriptor(), cachedWorkingSolution));
            } else {
                maximumValueCount =
                        Math.max(maximumValueCount, countOnEntity(variableDescriptor.getValueRangeDescriptor(), entity));
            }
        }
        return maximumValueCount;

    }

    private void processProblemScale(EntityDescriptor<Solution_> entityDescriptor, Object entity, ProblemScaleTracker tracker) {
        for (var variableDescriptor : entityDescriptor.getGenuineVariableDescriptorList()) {
            var valueCount = variableDescriptor.canExtractValueRangeFromSolution()
                    ? countOnSolution(variableDescriptor.getValueRangeDescriptor(), cachedWorkingSolution)
                    : countOnEntity(variableDescriptor.getValueRangeDescriptor(), entity);
            // TODO: When minimum Java supported is 21, this can be replaced with a sealed interface switch
            if (variableDescriptor instanceof BasicVariableDescriptor<Solution_> basicVariableDescriptor) {
                if (basicVariableDescriptor.isChained()) {
                    // An entity is a value
                    tracker.addListValueCount(1);
                    if (!entityDescriptor.isMovable(cachedWorkingSolution, entity)) {
                        tracker.addPinnedListValueCount(1);
                    }
                    // Anchors are entities
                    var valueRange = variableDescriptor.canExtractValueRangeFromSolution()
                            ? getFromSolution(variableDescriptor.getValueRangeDescriptor(), cachedWorkingSolution)
                            : getFromEntity(variableDescriptor.getValueRangeDescriptor(), entity);
                    var valueIterator = valueRange.createOriginalIterator();
                    while (valueIterator.hasNext()) {
                        var value = valueIterator.next();
                        if (variableDescriptor.isValuePotentialAnchor(value)) {
                            if (tracker.isAnchorVisited(value)) {
                                continue;
                            }
                            // Assumes anchors are not pinned
                            tracker.incrementListEntityCount(true);
                        }
                    }
                } else {
                    if (entityDescriptor.isMovable(cachedWorkingSolution, entity)) {
                        tracker.addBasicProblemScale(valueCount);
                    }
                }
            } else if (variableDescriptor instanceof ListVariableDescriptor<Solution_> listVariableDescriptor) {
                var size = variableDescriptor.canExtractValueRangeFromSolution()
                        ? countOnSolution(listVariableDescriptor.getValueRangeDescriptor(), cachedWorkingSolution)
                        : countOnEntity(listVariableDescriptor.getValueRangeDescriptor(), entity);
                tracker.setListTotalValueCount((int) size);
                if (entityDescriptor.isMovable(cachedWorkingSolution, entity)) {
                    tracker.incrementListEntityCount(true);
                    tracker.addPinnedListValueCount(listVariableDescriptor.getFirstUnpinnedIndex(entity));
                } else {
                    tracker.incrementListEntityCount(false);
                    tracker.addPinnedListValueCount(listVariableDescriptor.getListSize(entity));
                }
            } else {
                throw new IllegalStateException(
                        "Unhandled subclass of %s encountered (%s).".formatted(VariableDescriptor.class.getSimpleName(),
                                variableDescriptor.getClass().getSimpleName()));
            }
        }
    }

    /**
     * As {@link #getFromSolution(ValueRangeDescriptor, Object)}, but the solution is taken from the cached working solution.
     * This requires {@link #reset(Object)} to be called before the first call to this method,
     * and therefore this method will throw an exception if called before the score director is instantiated.
     *
     * @throws IllegalStateException if called before {@link #reset(Object)} is called
     */
    public <T> CountableValueRange<T> getFromSolution(ValueRangeDescriptor<Solution_> valueRangeDescriptor) {
        if (cachedWorkingSolution == null) {
            throw new IllegalStateException(
                    "Impossible state: value range (%s) requested before the working solution is known."
                            .formatted(valueRangeDescriptor));
        }
        return getFromSolution(valueRangeDescriptor, cachedWorkingSolution);
    }

    @SuppressWarnings("unchecked")
    public <T> CountableValueRange<T> getFromSolution(ValueRangeDescriptor<Solution_> valueRangeDescriptor,
            Solution_ solution) {
        var valueRange = fromSolutionMap.get(valueRangeDescriptor);
        if (valueRange == null) { // Avoid computeIfAbsent on the hot path; creates capturing lambda instances.
            var extractedValueRange = valueRangeDescriptor.<T> extractAllValues(Objects.requireNonNull(solution));
            if (!(extractedValueRange instanceof CountableValueRange<T> countableValueRange)) {
                throw new UnsupportedOperationException("""
                        Impossible state: value range (%s) on planning solution (%s) is not countable.
                        Maybe replace %s with %s."""
                        .formatted(valueRangeDescriptor, solution, DoubleValueRange.class.getSimpleName(),
                                BigDecimalValueRange.class.getSimpleName()));
            } else if (valueRangeDescriptor.acceptsNullInValueRange()) {
                valueRange = new NullAllowingCountableValueRange<>(countableValueRange);
            } else if (extractedValueRange instanceof EmptyValueRange<?>) {
                throw new IllegalStateException("""
                        The @%s-annotated member (%s) on planning solution (%s) must not return an empty range.
                        Maybe apply over-constrained planning as described in the documentation."""
                        .formatted(ValueRangeProvider.class.getSimpleName(), valueRangeDescriptor, solution));
            } else {
                valueRange = countableValueRange;
            }
            fromSolutionMap.put(valueRangeDescriptor, valueRange);
        }
        return (CountableValueRange<T>) valueRange;
    }

    /**
     * @throws IllegalStateException if called before {@link #reset(Object)} is called
     */
    @SuppressWarnings("unchecked")
    public <T> CountableValueRange<T> getFromEntity(ValueRangeDescriptor<Solution_> valueRangeDescriptor, Object entity) {
        if (cachedWorkingSolution == null) {
            throw new IllegalStateException(
                    "Impossible state: value range (%s) on planning entity (%s) requested before the working solution is known."
                            .formatted(valueRangeDescriptor, entity));
        }
        var valueRangeMap = fromEntityMap.computeIfAbsent(entity, e -> new IdentityHashMap<>());
        var valueRange = valueRangeMap.get(valueRangeDescriptor);
        if (valueRange == null) { // Avoid computeIfAbsent on the hot path; creates capturing lambda instances.
            var extractedValueRange =
                    valueRangeDescriptor.<T> extractValuesFromEntity(cachedWorkingSolution, Objects.requireNonNull(entity));
            if (!(extractedValueRange instanceof CountableValueRange<T> countableValueRange)) {
                throw new UnsupportedOperationException("""
                        Impossible state: value range (%s) on planning entity (%s) is not countable.
                        Maybe replace %s with %s."""
                        .formatted(valueRangeDescriptor, entity, DoubleValueRange.class.getSimpleName(),
                                BigDecimalValueRange.class.getSimpleName()));
            } else if (valueRangeDescriptor.acceptsNullInValueRange()) {
                valueRange = new NullAllowingCountableValueRange<>(countableValueRange);
            } else if (extractedValueRange instanceof EmptyValueRange<?>) {
                throw new IllegalStateException("""
                        The @%s-annotated member (%s) on planning entity (%s) must not return an empty range.
                        Maybe apply over-constrained planning as described in the documentation."""
                        .formatted(ValueRangeProvider.class.getSimpleName(), valueRangeDescriptor, entity));
            } else {
                valueRange = countableValueRange;
            }
            valueRangeMap.put(valueRangeDescriptor, valueRange);
        }
        return (CountableValueRange<T>) valueRange;
    }

    public long countOnSolution(ValueRangeDescriptor<Solution_> valueRangeDescriptor, Solution_ solution) {
        return getFromSolution(valueRangeDescriptor, solution)
                .getSize();
    }

    public long countOnEntity(ValueRangeDescriptor<Solution_> valueRangeDescriptor, Object entity) {
        return getFromEntity(valueRangeDescriptor, entity)
                .getSize();
    }

    public void reset(@Nullable Solution_ workingSolution) {
        fromSolutionMap.clear();
        fromEntityMap.clear();
        // We only update the cached solution if it is not null; null means to only reset the maps.
        if (workingSolution != null) {
            cachedWorkingSolution = workingSolution;
            cachedInitializationStatistics = null;
            cachedProblemSizeStatistics = null;
        }
    }

}
