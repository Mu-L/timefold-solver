package ai.timefold.solver.core.impl.move.streams.dataset.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ai.timefold.solver.core.impl.bavet.common.AbstractNodeBuildHelper;
import ai.timefold.solver.core.impl.bavet.common.tuple.AbstractTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.move.streams.dataset.AbstractDataStream;
import ai.timefold.solver.core.impl.move.streams.dataset.AbstractDatasetInstance;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DataNodeBuildHelper<Solution_> extends AbstractNodeBuildHelper<AbstractDataStream<Solution_>> {

    private final List<AbstractDatasetInstance<Solution_, ?>> datasetInstanceList = new ArrayList<>();

    public DataNodeBuildHelper(Set<AbstractDataStream<Solution_>> activeStreamSet) {
        super(activeStreamSet);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <Tuple_ extends AbstractTuple> void putInsertUpdateRetract(AbstractDataStream<Solution_> stream,
            TupleLifecycle<Tuple_> tupleLifecycle) {
        super.putInsertUpdateRetract(stream, tupleLifecycle);
        if (tupleLifecycle instanceof AbstractDatasetInstance datasetInstance) {
            datasetInstanceList.add(datasetInstance);
        }
    }

    public List<AbstractDatasetInstance<Solution_, ?>> getDatasetInstanceList() {
        return Collections.unmodifiableList(datasetInstanceList);
    }
}
