package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.AbstractDemandEnabledSelector;
import ai.timefold.solver.core.impl.heuristic.selector.common.SelectionCacheLifecycleBridge;
import ai.timefold.solver.core.impl.heuristic.selector.common.SelectionCacheLifecycleListener;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;

public abstract class AbstractCachingValueSelector<Solution_>
        extends AbstractDemandEnabledSelector<Solution_>
        implements SelectionCacheLifecycleListener<Solution_>, ValueSelector<Solution_> {

    protected final IterableValueSelector<Solution_> childValueSelector;
    protected final SelectionCacheType cacheType;

    protected List<Object> cachedValueList = null;

    protected AbstractCachingValueSelector(IterableValueSelector<Solution_> childValueSelector,
            SelectionCacheType cacheType) {
        this.childValueSelector = childValueSelector;
        this.cacheType = cacheType;
        if (childValueSelector.isNeverEnding()) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a childValueSelector (" + childValueSelector
                    + ") with neverEnding (" + childValueSelector.isNeverEnding() + ").");
        }
        phaseLifecycleSupport.addEventListener(childValueSelector);
        if (cacheType.isNotCached()) {
            throw new IllegalArgumentException("The selector (" + this
                    + ") does not support the cacheType (" + cacheType + ").");
        }
        phaseLifecycleSupport.addEventListener(new SelectionCacheLifecycleBridge<>(cacheType, this));
    }

    public ValueSelector<Solution_> getChildValueSelector() {
        return childValueSelector;
    }

    @Override
    public SelectionCacheType getCacheType() {
        return cacheType;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void constructCache(SolverScope<Solution_> solverScope) {
        long childSize = childValueSelector.getSize();
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The selector (" + this
                    + ") has a childValueSelector (" + childValueSelector
                    + ") with childSize (" + childSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        cachedValueList = new ArrayList<>((int) childSize);
        // TODO Fail-faster if a non FromSolutionPropertyValueSelector is used
        childValueSelector.iterator().forEachRemaining(cachedValueList::add);
        logger.trace("    Created cachedValueList: size ({}), valueSelector ({}).",
                cachedValueList.size(), this);
    }

    @Override
    public void disposeCache(SolverScope<Solution_> solverScope) {
        cachedValueList = null;
    }

    @Override
    public GenuineVariableDescriptor<Solution_> getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    @Override
    public boolean isCountable() {
        return true;
    }

    @Override
    public long getSize(Object entity) {
        return getSize();
    }

    public long getSize() {
        return cachedValueList.size();
    }

    @Override
    public Iterator<Object> endingIterator(Object entity) {
        return endingIterator();
    }

    public Iterator<Object> endingIterator() {
        return cachedValueList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        AbstractCachingValueSelector<?> that = (AbstractCachingValueSelector<?>) other;
        return Objects.equals(childValueSelector, that.childValueSelector) && cacheType == that.cacheType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(childValueSelector, cacheType);
    }
}
