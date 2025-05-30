package ai.timefold.solver.core.impl.score.stream.collector.bi;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import ai.timefold.solver.core.impl.score.stream.collector.MinMaxUndoableActionable;

import org.jspecify.annotations.NonNull;

final class MinPropertyBiCollector<A, B, Result_, Property_ extends Comparable<? super Property_>>
        extends UndoableActionableBiCollector<A, B, Result_, Result_, MinMaxUndoableActionable<Result_, Property_>> {
    private final Function<? super Result_, ? extends Property_> propertyMapper;

    MinPropertyBiCollector(BiFunction<? super A, ? super B, ? extends Result_> mapper,
            Function<? super Result_, ? extends Property_> propertyMapper) {
        super(mapper);
        this.propertyMapper = propertyMapper;
    }

    @Override
    public @NonNull Supplier<MinMaxUndoableActionable<Result_, Property_>> supplier() {
        return () -> MinMaxUndoableActionable.minCalculator(propertyMapper);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        MinPropertyBiCollector<?, ?, ?, ?> that = (MinPropertyBiCollector<?, ?, ?, ?>) object;
        return Objects.equals(propertyMapper, that.propertyMapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyMapper);
    }
}
