package ai.timefold.solver.core.impl.heuristic.selector.move.generic;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ai.timefold.solver.core.impl.domain.variable.descriptor.BasicVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import ai.timefold.solver.core.impl.heuristic.selector.entity.pillar.PillarSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;

public class PillarChangeMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    protected final PillarSelector<Solution_> pillarSelector;
    protected final ValueSelector<Solution_> valueSelector;
    protected final boolean randomSelection;

    public PillarChangeMoveSelector(PillarSelector<Solution_> pillarSelector, ValueSelector<Solution_> valueSelector,
            boolean randomSelection) {
        this.pillarSelector = pillarSelector;
        this.valueSelector = valueSelector;
        this.randomSelection = randomSelection;
        GenuineVariableDescriptor<Solution_> variableDescriptor = valueSelector.getVariableDescriptor();
        boolean isChained = variableDescriptor instanceof BasicVariableDescriptor<Solution_> basicVariableDescriptor
                && basicVariableDescriptor.isChained();
        if (isChained) {
            throw new IllegalStateException("The selector (%s) has a variableDescriptor (%s) which is chained (%s)."
                    .formatted(this, variableDescriptor, isChained));
        }
        phaseLifecycleSupport.addEventListener(pillarSelector);
        phaseLifecycleSupport.addEventListener(valueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isCountable() {
        return pillarSelector.isCountable() && valueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || pillarSelector.isNeverEnding() || valueSelector.isNeverEnding();
    }

    @Override
    public long getSize() {
        return pillarSelector.getSize() * ((IterableValueSelector<Solution_>) valueSelector).getSize();
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        if (!randomSelection) {
            return new OriginalPillarChangeMoveIterator();
        } else {
            return new RandomPillarChangeMoveIterator();
        }
    }

    private class OriginalPillarChangeMoveIterator extends UpcomingSelectionIterator<Move<Solution_>> {

        private Iterator<List<Object>> pillarIterator;
        private Iterator<Object> valueIterator;

        private List<Object> upcomingPillar;

        private OriginalPillarChangeMoveIterator() {
            pillarIterator = pillarSelector.iterator();
            // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
            valueIterator = Collections.emptyIterator();
        }

        @Override
        protected Move<Solution_> createUpcomingSelection() {
            if (!valueIterator.hasNext()) {
                if (!pillarIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                upcomingPillar = pillarIterator.next();
                valueIterator = valueSelector.iterator(upcomingPillar.get(0));
                if (!valueIterator.hasNext()) {
                    // valueSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            Object toValue = valueIterator.next();

            return new PillarChangeMove<>(upcomingPillar, valueSelector.getVariableDescriptor(), toValue);
        }

    }

    private class RandomPillarChangeMoveIterator extends UpcomingSelectionIterator<Move<Solution_>> {

        private Iterator<List<Object>> pillarIterator;
        private Iterator<Object> valueIterator;

        private RandomPillarChangeMoveIterator() {
            pillarIterator = pillarSelector.iterator();
            // Don't do hasNext() in constructor (to avoid upcoming selections breaking mimic recording)
            valueIterator = Collections.emptyIterator();
        }

        @Override
        protected Move<Solution_> createUpcomingSelection() {
            // Ideally, this code should have read:
            //     Object pillar = pillarIterator.next();
            //     Object toValue = valueIterator.next();
            // But empty selectors and ending selectors (such as non-random or shuffled) make it more complex
            if (!pillarIterator.hasNext()) {
                pillarIterator = pillarSelector.iterator();
                if (!pillarIterator.hasNext()) {
                    // pillarSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            List<Object> pillar = pillarIterator.next();

            if (!valueIterator.hasNext()) {
                valueIterator = valueSelector.iterator(pillar.get(0));
                if (!valueIterator.hasNext()) {
                    // valueSelector is completely empty
                    return noUpcomingSelection();
                }
            }
            Object toValue = valueIterator.next();

            return new PillarChangeMove<>(pillar, valueSelector.getVariableDescriptor(), toValue);
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pillarSelector + ", " + valueSelector + ")";
    }

}
