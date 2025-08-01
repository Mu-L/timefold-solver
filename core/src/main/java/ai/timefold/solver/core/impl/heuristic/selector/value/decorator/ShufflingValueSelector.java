package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import java.util.Collections;
import java.util.Iterator;

import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;

public final class ShufflingValueSelector<Solution_>
        extends AbstractCachingValueSelector<Solution_>
        implements IterableValueSelector<Solution_> {

    public ShufflingValueSelector(IterableValueSelector<Solution_> childValueSelector,
            SelectionCacheType cacheType) {
        super(childValueSelector, cacheType);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isNeverEnding() {
        return false;
    }

    @Override
    public Iterator<Object> iterator(Object entity) {
        return iterator();
    }

    @Override
    public Iterator<Object> iterator() {
        Collections.shuffle(cachedValueList, workingRandom);
        logger.trace("    Shuffled cachedValueList with size ({}) in valueSelector({}).",
                cachedValueList.size(), this);
        return cachedValueList.iterator();
    }

    @Override
    public String toString() {
        return "Shuffling(" + childValueSelector + ")";
    }

}
