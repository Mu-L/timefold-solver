package ai.timefold.solver.core.impl.score.stream.collector.tri;

import java.util.function.Supplier;

import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.impl.score.stream.collector.MinMaxUndoableActionable;

import org.jspecify.annotations.NonNull;

final class MaxComparableTriCollector<A, B, C, Result_ extends Comparable<? super Result_>>
        extends UndoableActionableTriCollector<A, B, C, Result_, Result_, MinMaxUndoableActionable<Result_, Result_>> {
    MaxComparableTriCollector(TriFunction<? super A, ? super B, ? super C, ? extends Result_> mapper) {
        super(mapper);
    }

    @Override
    public @NonNull Supplier<MinMaxUndoableActionable<Result_, Result_>> supplier() {
        return MinMaxUndoableActionable::maxCalculator;
    }
}
