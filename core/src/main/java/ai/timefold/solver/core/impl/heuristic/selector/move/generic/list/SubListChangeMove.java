package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.heuristic.selector.list.SubList;
import ai.timefold.solver.core.impl.score.director.ValueRangeManager;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;
import ai.timefold.solver.core.impl.util.CollectionUtils;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class SubListChangeMove<Solution_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> variableDescriptor;
    private final Object sourceEntity;
    private final int sourceIndex;
    private final int length;
    private final Object destinationEntity;
    private final int destinationIndex;
    private final boolean reversing;

    private Collection<Object> planningValues;

    public SubListChangeMove(ListVariableDescriptor<Solution_> variableDescriptor, SubList subList, Object destinationEntity,
            int destinationIndex, boolean reversing) {
        this(variableDescriptor, subList.entity(), subList.fromIndex(), subList.length(), destinationEntity,
                destinationIndex, reversing);
    }

    public SubListChangeMove(
            ListVariableDescriptor<Solution_> variableDescriptor,
            Object sourceEntity, int sourceIndex, int length,
            Object destinationEntity, int destinationIndex, boolean reversing) {
        this.variableDescriptor = variableDescriptor;
        this.sourceEntity = sourceEntity;
        this.sourceIndex = sourceIndex;
        this.length = length;
        this.destinationEntity = destinationEntity;
        this.destinationIndex = destinationIndex;
        this.reversing = reversing;
    }

    public Object getSourceEntity() {
        return sourceEntity;
    }

    public int getFromIndex() {
        return sourceIndex;
    }

    public int getSubListSize() {
        return length;
    }

    public int getToIndex() {
        return sourceIndex + length;
    }

    public boolean isReversing() {
        return reversing;
    }

    public Object getDestinationEntity() {
        return destinationEntity;
    }

    public int getDestinationIndex() {
        return destinationIndex;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        var sameEntity = destinationEntity == sourceEntity;
        if (sameEntity && destinationIndex == sourceIndex) {
            return false;
        }
        var doable = !sameEntity || destinationIndex + length <= variableDescriptor.getListSize(destinationEntity);
        if (!doable || sameEntity || variableDescriptor.canExtractValueRangeFromSolution()) {
            return doable;
        }
        // When the first and second elements are different,
        // and the value range is located at the entity,
        // we need to check if the destination's value range accepts the upcoming values
        ValueRangeManager<Solution_> valueRangeManager =
                ((VariableDescriptorAwareScoreDirector<Solution_>) scoreDirector).getValueRangeManager();
        var destinationValueRange =
                valueRangeManager.getFromEntity(variableDescriptor.getValueRangeDescriptor(),
                        destinationEntity);
        var sourceList = variableDescriptor.getValue(sourceEntity);
        var subList = sourceList.subList(sourceIndex, sourceIndex + length);
        return subList.stream().allMatch(destinationValueRange::contains);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        var castScoreDirector = (VariableDescriptorAwareScoreDirector<Solution_>) scoreDirector;

        var sourceList = variableDescriptor.getValue(sourceEntity);
        var subList = sourceList.subList(sourceIndex, sourceIndex + length);
        planningValues = CollectionUtils.copy(subList, reversing);

        if (sourceEntity == destinationEntity) {
            var fromIndex = Math.min(sourceIndex, destinationIndex);
            var toIndex = Math.max(sourceIndex, destinationIndex) + length;
            castScoreDirector.beforeListVariableChanged(variableDescriptor, sourceEntity, fromIndex, toIndex);
            subList.clear();
            variableDescriptor.getValue(destinationEntity).addAll(destinationIndex, planningValues);
            castScoreDirector.afterListVariableChanged(variableDescriptor, sourceEntity, fromIndex, toIndex);
        } else {
            castScoreDirector.beforeListVariableChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex + length);
            subList.clear();
            castScoreDirector.afterListVariableChanged(variableDescriptor, sourceEntity, sourceIndex, sourceIndex);
            castScoreDirector.beforeListVariableChanged(variableDescriptor, destinationEntity, destinationIndex,
                    destinationIndex);
            variableDescriptor.getValue(destinationEntity).addAll(destinationIndex, planningValues);
            castScoreDirector.afterListVariableChanged(variableDescriptor, destinationEntity, destinationIndex,
                    destinationIndex + length);
        }
    }

    @Override
    public SubListChangeMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new SubListChangeMove<>(
                variableDescriptor,
                destinationScoreDirector.lookUpWorkingObject(sourceEntity), sourceIndex, length,
                destinationScoreDirector.lookUpWorkingObject(destinationEntity), destinationIndex,
                reversing);
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<Object> getPlanningEntities() {
        // Use LinkedHashSet for predictable iteration order.
        Set<Object> entities = new LinkedHashSet<>(2);
        entities.add(sourceEntity);
        entities.add(destinationEntity);
        return entities;
    }

    @Override
    public Collection<Object> getPlanningValues() {
        return planningValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubListChangeMove<?> other = (SubListChangeMove<?>) o;
        return sourceIndex == other.sourceIndex && length == other.length
                && destinationIndex == other.destinationIndex && reversing == other.reversing
                && variableDescriptor.equals(other.variableDescriptor)
                && sourceEntity.equals(other.sourceEntity)
                && destinationEntity.equals(other.destinationEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableDescriptor, sourceEntity, sourceIndex, length, destinationEntity, destinationIndex,
                reversing);
    }

    @Override
    public String toString() {
        return String.format("|%d| {%s[%d..%d] -%s> %s[%d]}",
                length, sourceEntity, sourceIndex, getToIndex(),
                reversing ? "reversing-" : "", destinationEntity, destinationIndex);
    }
}
