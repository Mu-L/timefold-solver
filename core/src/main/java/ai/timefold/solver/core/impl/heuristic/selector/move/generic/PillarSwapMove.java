package ai.timefold.solver.core.impl.heuristic.selector.move.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.AbstractMove;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.score.director.VariableDescriptorAwareScoreDirector;
import ai.timefold.solver.core.impl.util.CollectionUtils;

/**
 * This {@link Move} is not cacheable.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class PillarSwapMove<Solution_> extends AbstractMove<Solution_> {

    protected final List<GenuineVariableDescriptor<Solution_>> variableDescriptorList;

    protected final List<Object> leftPillar;
    protected final List<Object> rightPillar;

    public PillarSwapMove(List<GenuineVariableDescriptor<Solution_>> variableDescriptorList,
            List<Object> leftPillar, List<Object> rightPillar) {
        this.variableDescriptorList = variableDescriptorList;
        this.leftPillar = leftPillar;
        this.rightPillar = rightPillar;
    }

    public List<String> getVariableNameList() {
        List<String> variableNameList = new ArrayList<>(variableDescriptorList.size());
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            variableNameList.add(variableDescriptor.getVariableName());
        }
        return variableNameList;
    }

    public List<Object> getLeftPillar() {
        return leftPillar;
    }

    public List<Object> getRightPillar() {
        return rightPillar;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        var movable = false;
        for (var variableDescriptor : variableDescriptorList) {
            var leftValue = variableDescriptor.getValue(leftPillar.get(0));
            var rightValue = variableDescriptor.getValue(rightPillar.get(0));
            if (!Objects.equals(leftValue, rightValue)) {
                movable = true;
                if (!variableDescriptor.canExtractValueRangeFromSolution()) {
                    var valueRangeDescriptor = variableDescriptor.getValueRangeDescriptor();
                    for (var rightEntity : rightPillar) {
                        var rightValueRange =
                                extractValueRangeFromEntity(scoreDirector, valueRangeDescriptor, rightEntity);
                        if (!rightValueRange.contains(leftValue)) {
                            return false;
                        }
                    }
                    for (var leftEntity : leftPillar) {
                        var leftValueRange =
                                extractValueRangeFromEntity(scoreDirector, valueRangeDescriptor, leftEntity);
                        if (!leftValueRange.contains(rightValue)) {
                            return false;
                        }
                    }
                }
            }
        }
        return movable;
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        var castScoreDirector = (VariableDescriptorAwareScoreDirector<Solution_>) scoreDirector;
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            Object oldLeftValue = variableDescriptor.getValue(leftPillar.get(0));
            Object oldRightValue = variableDescriptor.getValue(rightPillar.get(0));
            if (!Objects.equals(oldLeftValue, oldRightValue)) {
                for (Object leftEntity : leftPillar) {
                    castScoreDirector.changeVariableFacade(variableDescriptor, leftEntity, oldRightValue);
                }
                for (Object rightEntity : rightPillar) {
                    castScoreDirector.changeVariableFacade(variableDescriptor, rightEntity, oldLeftValue);
                }
            }
        }
    }

    @Override
    public PillarSwapMove<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        return new PillarSwapMove<>(variableDescriptorList,
                rebaseList(leftPillar, destinationScoreDirector), rebaseList(rightPillar, destinationScoreDirector));
    }

    // ************************************************************************
    // Introspection methods
    // ************************************************************************

    @Override
    public String getSimpleMoveTypeDescription() {
        StringBuilder moveTypeDescription = new StringBuilder(20 * (variableDescriptorList.size() + 1));
        moveTypeDescription.append(getClass().getSimpleName()).append("(");
        String delimiter = "";
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            moveTypeDescription.append(delimiter).append(variableDescriptor.getSimpleEntityAndVariableName());
            delimiter = ", ";
        }
        moveTypeDescription.append(")");
        return moveTypeDescription.toString();
    }

    @Override
    public Collection<? extends Object> getPlanningEntities() {
        return CollectionUtils.concat(leftPillar, rightPillar);
    }

    @Override
    public Collection<? extends Object> getPlanningValues() {
        List<Object> values = new ArrayList<>(variableDescriptorList.size() * 2);
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            values.add(variableDescriptor.getValue(leftPillar.get(0)));
            values.add(variableDescriptor.getValue(rightPillar.get(0)));
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PillarSwapMove<?> other = (PillarSwapMove<?>) o;
        return Objects.equals(variableDescriptorList, other.variableDescriptorList) &&
                Objects.equals(leftPillar, other.leftPillar) &&
                Objects.equals(rightPillar, other.rightPillar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableDescriptorList, leftPillar, rightPillar);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(variableDescriptorList.size() * 16);
        s.append(leftPillar).append(" {");
        appendVariablesToString(s, leftPillar);
        s.append("} <-> ");
        s.append(rightPillar).append(" {");
        appendVariablesToString(s, rightPillar);
        s.append("}");
        return s.toString();
    }

    protected void appendVariablesToString(StringBuilder s, List<Object> pillar) {
        boolean first = true;
        for (GenuineVariableDescriptor<Solution_> variableDescriptor : variableDescriptorList) {
            if (!first) {
                s.append(", ");
            }
            var value = variableDescriptor.getValue(pillar.get(0));
            s.append(value == null ? null : value.toString());
            first = false;
        }
    }

}
