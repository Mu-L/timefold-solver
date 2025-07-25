package ai.timefold.solver.core.impl.heuristic.selector.move.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;

/**
 * This {@link Move} is not cacheable.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class PillarChangeMove<Solution_> extends AbstractMove<Solution_> {

    protected final GenuineVariableDescriptor<Solution_> variableDescriptor;

    protected final List<Object> pillar;
    protected final Object toPlanningValue;

    public PillarChangeMove(List<Object> pillar, GenuineVariableDescriptor<Solution_> variableDescriptor,
            Object toPlanningValue) {
        this.pillar = pillar;
        this.variableDescriptor = variableDescriptor;
        this.toPlanningValue = toPlanningValue;
    }

    public List<Object> getPillar() {
        return pillar;
    }

    public String getVariableName() {
        return variableDescriptor.getVariableName();
    }

    public Object getToPlanningValue() {
        return toPlanningValue;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        var oldValue = variableDescriptor.getValue(pillar.get(0));
        if (Objects.equals(oldValue, toPlanningValue)) {
            return false;
        }
        if (!variableDescriptor.canExtractValueRangeFromSolution()) {
            var valueRangeDescriptor = variableDescriptor.getValueRangeDescriptor();
            for (Object entity : pillar) {
                var rightValueRange = extractValueRangeFromEntity(scoreDirector, valueRangeDescriptor, entity);
                if (!rightValueRange.contains(toPlanningValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        var castScoreDirector = (VariableDescriptorAwareScoreDirector<Solution_>) scoreDirector;
        for (var entity : pillar) {
            castScoreDirector.changeVariableFacade(variableDescriptor, entity, toPlanningValue);
        }
    }

    @Override
    public PillarChangeMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new PillarChangeMove<>(rebaseList(pillar, destinationScoreDirector), variableDescriptor,
                destinationScoreDirector.lookUpWorkingObject(toPlanningValue));
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        return getClass().getSimpleName() + "(" + variableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return pillar;
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toPlanningValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PillarChangeMove<?> other = (PillarChangeMove<?>) o;
        return Objects.equals(variableDescriptor, other.variableDescriptor) &&
                Objects.equals(pillar, other.pillar) &&
                Objects.equals(toPlanningValue, other.toPlanningValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableDescriptor, pillar, toPlanningValue);
    }

    @Override
    public String toString() {
        Object oldValue = variableDescriptor.getValue(pillar.get(0));
        return pillar + " {" + oldValue + " -> " + toPlanningValue + "}";
    }

}
