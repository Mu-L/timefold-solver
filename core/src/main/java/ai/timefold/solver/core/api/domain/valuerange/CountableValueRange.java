package ai.timefold.solver.core.api.domain.valuerange;

import java.util.Iterator;

import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueRange} that is ending. Therefore, it has a discrete (as in non-continuous) range.
 * <p>
 * Don't implement this interface directly.
 * If you can't use a collection to store the values,
 * use {@link ValueRangeFactory} to get an instance of a {@link CountableValueRange}.
 * 
 * @see ValueRangeFactory
 * @see ValueRange
 */
@NullMarked
public interface CountableValueRange<T> extends ValueRange<T> {

    /**
     * Used by uniform random selection in a composite CountableValueRange,
     * or one which includes nulls.
     *
     * @return the exact number of elements generated by this {@link CountableValueRange}, always {@code >= 0}
     */
    long getSize();

    /**
     * Used by uniform random selection in a composite CountableValueRange,
     * or one which includes nulls.
     *
     * @param index always {@code <} {@link #getSize()}
     * @return sometimes null (if {@link PlanningVariable#allowsUnassigned()} is true)
     */
    @Nullable
    T get(long index);

    /**
     * Select the elements in original (natural) order.
     */
    Iterator<T> createOriginalIterator();

}
