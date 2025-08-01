package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import java.util.Iterator;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.AbstractDemandEnabledSelector;
import ai.timefold.solver.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;

/**
 * Prevents creating chains without an anchor.
 * <p>
 * Filters out every value that is a planning entity for which the {@link PlanningVariable}
 * (on which this {@link ValueSelector} applies to) is uninitialized.
 * <p>
 * Mainly used for chained planning variables, but supports other planning variables too.
 */
public class InitializedValueSelector<Solution_>
        extends AbstractDemandEnabledSelector<Solution_>
        implements ValueSelector<Solution_> {

    public static <Solution_> ValueSelector<Solution_> create(ValueSelector<Solution_> valueSelector) {
        if (valueSelector instanceof IterableValueSelector) {
            return new IterableInitializedValueSelector<>((IterableValueSelector<Solution_>) valueSelector);
        } else {
            return new InitializedValueSelector<>(valueSelector);
        }
    }

    private final GenuineVariableDescriptor<Solution_> variableDescriptor;
    final ValueSelector<Solution_> childValueSelector;
    final boolean bailOutEnabled;

    InitializedValueSelector(ValueSelector<Solution_> childValueSelector) {
        this.variableDescriptor = childValueSelector.getVariableDescriptor();
        this.childValueSelector = childValueSelector;
        bailOutEnabled = childValueSelector.isNeverEnding();
        phaseLifecycleSupport.addEventListener(childValueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    @Override
    public boolean isCountable() {
        return childValueSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return childValueSelector.isNeverEnding();
    }

    @Override
    public long getSize(Object entity) {
        // TODO use cached results
        return childValueSelector.getSize(entity);
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        return new JustInTimeInitializedValueIterator(entity, childValueSelector.iterator(entity));
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        return new JustInTimeInitializedValueIterator(entity, childValueSelector.endingIterator(entity));
    }

    protected class JustInTimeInitializedValueIterator extends UpcomingSelectionIterator<Object> {

        private final Iterator<Object> childValueIterator;
        private final long bailOutSize;

        public JustInTimeInitializedValueIterator(Object entity, Iterator<Object> childValueIterator) {
            this(childValueIterator, determineBailOutSize(entity));
        }

        public JustInTimeInitializedValueIterator(Iterator<Object> childValueIterator, long bailOutSize) {
            this.childValueIterator = childValueIterator;
            this.bailOutSize = bailOutSize;
        }

        @Override
        protected Object createUpcomingSelection() {
            Object next;
            long attemptsBeforeBailOut = bailOutSize;
            do {
                if (!childValueIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                if (bailOutEnabled) {
                    // if childValueIterator is neverEnding and nothing is accepted, bail out of the infinite loop
                    if (attemptsBeforeBailOut <= 0L) {
                        logger.trace("Bailing out of neverEnding selector ({}) to avoid infinite loop.",
                                InitializedValueSelector.this);
                        return noUpcomingSelection();
                    }
                    attemptsBeforeBailOut--;
                }
                next = childValueIterator.next();
            } while (!accept(next));
            return next;
        }

    }

    protected long determineBailOutSize(Object entity) {
        if (!bailOutEnabled) {
            return -1L;
        }
        return childValueSelector.getSize(entity) * 10L;
    }

    protected boolean accept(Object value) {
        return value == null
                || !variableDescriptor.getEntityDescriptor().getEntityClass().isAssignableFrom(value.getClass())
                || variableDescriptor.isInitialized(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        InitializedValueSelector<?> that = (InitializedValueSelector<?>) other;
        return Objects.equals(variableDescriptor, that.variableDescriptor)
                && Objects.equals(childValueSelector, that.childValueSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableDescriptor, childValueSelector);
    }

    @Override
    public String toString() {
        return "Initialized(" + childValueSelector + ")";
    }

}
