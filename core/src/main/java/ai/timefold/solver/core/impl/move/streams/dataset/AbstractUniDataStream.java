package ai.timefold.solver.core.impl.move.streams.dataset;

import java.util.function.Predicate;

import ai.timefold.solver.core.api.score.stream.bi.BiJoiner;
import ai.timefold.solver.core.impl.bavet.bi.joiner.BiJoinerComber;
import ai.timefold.solver.core.impl.move.streams.dataset.common.bridge.ForeBridgeUniDataStream;
import ai.timefold.solver.core.impl.move.streams.maybeapi.stream.BiDataStream;
import ai.timefold.solver.core.impl.move.streams.maybeapi.stream.UniDataStream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class AbstractUniDataStream<Solution_, A> extends AbstractDataStream<Solution_>
        implements UniDataStream<Solution_, A> {

    protected AbstractUniDataStream(DataStreamFactory<Solution_> dataStreamFactory) {
        super(dataStreamFactory, null);
    }

    protected AbstractUniDataStream(DataStreamFactory<Solution_> dataStreamFactory,
            @Nullable AbstractDataStream<Solution_> parent) {
        super(dataStreamFactory, parent);
    }

    @Override
    public final UniDataStream<Solution_, A> filter(Predicate<A> predicate) {
        return shareAndAddChild(new FilterUniDataStream<>(dataStreamFactory, this, predicate));
    }

    @Override
    public @NonNull <B> BiDataStream<Solution_, A, B> join(@NonNull UniDataStream<Solution_, B> otherStream,
            @NonNull BiJoiner<A, B>... joiners) {
        var other = (AbstractUniDataStream<Solution_, B>) otherStream;
        var leftBridge = new ForeBridgeUniDataStream<Solution_, A>(dataStreamFactory, this);
        var rightBridge = new ForeBridgeUniDataStream<Solution_, B>(dataStreamFactory, other);
        var joinerComber = BiJoinerComber.comb(joiners);
        var joinStream = new JoinBiDataStream<>(dataStreamFactory, leftBridge, rightBridge,
                joinerComber.getMergedJoiner(), joinerComber.getMergedFiltering());
        return dataStreamFactory.share(joinStream, joinStream_ -> {
            // Connect the bridges upstream, as it is an actual new join.
            getChildStreamList().add(leftBridge);
            other.getChildStreamList().add(rightBridge);
        });
    }

    @Override
    public @NonNull <B> BiDataStream<Solution_, A, B> join(@NonNull Class<B> otherClass, @NonNull BiJoiner<A, B>... joiners) {
        return join(dataStreamFactory.forEachNonDiscriminating(otherClass, false), joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniDataStream<Solution_, A> ifExists(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExists(dataStreamFactory.forEachNonDiscriminating(otherClass, false), joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniDataStream<Solution_, A> ifExists(UniDataStream<Solution_, B> otherStream, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(true, otherStream, joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniDataStream<Solution_, A> ifNotExists(Class<B> otherClass, BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(false, dataStreamFactory.forEachNonDiscriminating(otherClass, false), joiners);
    }

    @SafeVarargs
    @Override
    public final <B> UniDataStream<Solution_, A> ifNotExists(UniDataStream<Solution_, B> otherStream,
            BiJoiner<A, B>... joiners) {
        return ifExistsOrNot(false, otherStream, joiners);
    }

    private <B> UniDataStream<Solution_, A> ifExistsOrNot(boolean shouldExist, UniDataStream<Solution_, B> otherStream,
            BiJoiner<A, B>[] joiners) {
        var other = (AbstractUniDataStream<Solution_, B>) otherStream;
        var joinerComber = BiJoinerComber.comb(joiners);
        var parentBridgeB = other.shareAndAddChild(new ForeBridgeUniDataStream<Solution_, B>(dataStreamFactory, other));
        return dataStreamFactory.share(new IfExistsUniDataStream<>(dataStreamFactory, this, parentBridgeB, shouldExist,
                joinerComber.getMergedJoiner(), joinerComber.getMergedFiltering()), childStreamList::add);
    }

    public UniDataset<Solution_, A> createDataset() {
        var stream = shareAndAddChild(new TerminalUniDataStream<>(dataStreamFactory, this));
        return stream.getDataset();
    }

}
