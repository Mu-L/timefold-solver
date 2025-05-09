package ai.timefold.solver.core.impl.score.stream.bavet.bi;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.impl.bavet.bi.ConcatBiUniNode;
import ai.timefold.solver.core.impl.bavet.bi.ConcatUniBiNode;
import ai.timefold.solver.core.impl.bavet.common.AbstractConcatNode;
import ai.timefold.solver.core.impl.bavet.common.BavetAbstractConstraintStream;
import ai.timefold.solver.core.impl.bavet.common.tuple.BiTuple;
import ai.timefold.solver.core.impl.bavet.common.tuple.TupleLifecycle;
import ai.timefold.solver.core.impl.score.stream.bavet.BavetConstraintFactory;
import ai.timefold.solver.core.impl.score.stream.bavet.common.BavetConcatConstraintStream;
import ai.timefold.solver.core.impl.score.stream.bavet.common.ConstraintNodeBuildHelper;
import ai.timefold.solver.core.impl.score.stream.bavet.common.bridge.BavetForeBridgeBiConstraintStream;
import ai.timefold.solver.core.impl.score.stream.bavet.common.bridge.BavetForeBridgeUniConstraintStream;

public final class BavetUniConcatBiConstraintStream<Solution_, A, B> extends BavetAbstractBiConstraintStream<Solution_, A, B>
        implements BavetConcatConstraintStream<Solution_> {

    private final BavetAbstractConstraintStream<Solution_> leftParent;
    private final BavetAbstractConstraintStream<Solution_> rightParent;
    private final Function<A, B> paddingFunction;
    private final ConcatNodeConstructor<A, B> nodeConstructor;

    public BavetUniConcatBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetForeBridgeUniConstraintStream<Solution_, A> leftParent,
            BavetForeBridgeBiConstraintStream<Solution_, A, B> rightParent,
            Function<A, B> paddingFunction) {
        super(constraintFactory, leftParent.getRetrievalSemantics());
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.paddingFunction = paddingFunction;
        this.nodeConstructor = ConcatUniBiNode::new;
    }

    public BavetUniConcatBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetForeBridgeBiConstraintStream<Solution_, A, B> leftParent,
            BavetForeBridgeUniConstraintStream<Solution_, A> rightParent,
            Function<A, B> paddingFunction) {
        super(constraintFactory, leftParent.getRetrievalSemantics());
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.paddingFunction = paddingFunction;
        this.nodeConstructor = ConcatBiUniNode::new;
    }

    @Override
    public boolean guaranteesDistinct() {
        /*
         * Since one of the two parents is increasing in cardinality,
         * it means its tuples must be distinct from the other parent's tuples.
         * Therefore, the guarantee can be given is both of the parents give it.
         */
        return leftParent.guaranteesDistinct() && rightParent.guaranteesDistinct();
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        leftParent.collectActiveConstraintStreams(constraintStreamSet);
        rightParent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(ConstraintNodeBuildHelper<Solution_, Score_> buildHelper) {
        TupleLifecycle<BiTuple<A, B>> downstream = buildHelper.getAggregatedTupleLifecycle(childStreamList);
        var leftCloneStoreIndex = buildHelper.reserveTupleStoreIndex(leftParent.getTupleSource());
        var rightCloneStoreIndex = buildHelper.reserveTupleStoreIndex(rightParent.getTupleSource());
        var outputStoreSize = buildHelper.extractTupleStoreSize(this);
        var node =
                nodeConstructor.apply(paddingFunction, downstream, leftCloneStoreIndex, rightCloneStoreIndex, outputStoreSize);
        buildHelper.addNode(node, this, leftParent, rightParent);
    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var other = (BavetUniConcatBiConstraintStream<?, ?, ?>) o;
        /*
         * Bridge streams do not implement equality because their equals() would have to point back to this stream,
         * resulting in StackOverflowError.
         * Therefore we need to check bridge parents to see where this concat node comes from.
         */
        return Objects.equals(leftParent.getParent(), other.leftParent.getParent())
                && Objects.equals(rightParent.getParent(), other.rightParent.getParent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftParent.getParent(), rightParent.getParent());
    }

    @Override
    public String toString() {
        return "UniConcat() with " + childStreamList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public BavetAbstractConstraintStream<Solution_> getLeftParent() {
        return leftParent;
    }

    @Override
    public BavetAbstractConstraintStream<Solution_> getRightParent() {
        return rightParent;
    }

    private interface ConcatNodeConstructor<A, B> {

        AbstractConcatNode<?, ?, ?> apply(Function<A, B> paddingFunction,
                TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle,
                int leftCloneStoreIndex, int rightCloneStoreIndex, int outputStoreSize);

    }

}
