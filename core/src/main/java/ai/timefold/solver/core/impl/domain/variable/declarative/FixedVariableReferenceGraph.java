package ai.timefold.solver.core.impl.domain.variable.declarative;

import java.util.BitSet;
import java.util.PriorityQueue;
import java.util.function.IntFunction;

import org.jspecify.annotations.NonNull;

public final class FixedVariableReferenceGraph<Solution_>
        extends AbstractVariableReferenceGraph<Solution_, PriorityQueue<BaseTopologicalOrderGraph.NodeTopologicalOrder>> {
    // These are immutable
    private final ChangedVariableNotifier<Solution_> changedVariableNotifier;

    // These are mutable
    private boolean isFinalized = false;

    public FixedVariableReferenceGraph(VariableReferenceGraphBuilder<Solution_> outerGraph,
            IntFunction<TopologicalOrderGraph> graphCreator) {
        super(outerGraph, graphCreator);
        // We don't use a bit set to store changes, so pass a one-use instance
        graph.commitChanges(new BitSet(instanceList.size()));
        isFinalized = true;

        // Now that we know the topological order of nodes, add
        // each node to changed.
        for (var node = 0; node < instanceList.size(); node++) {
            changeSet.add(graph.getTopologicalOrder(node));
        }
        changedVariableNotifier = outerGraph.changedVariableNotifier;
    }

    @Override
    protected PriorityQueue<BaseTopologicalOrderGraph.NodeTopologicalOrder> createChangeSet(int instanceCount) {
        return new PriorityQueue<>(instanceCount);
    }

    @Override
    public void markChanged(@NonNull EntityVariablePair<Solution_> node) {
        // Before the graph is finalized, ignore changes, since
        // we don't know the topological order yet
        if (isFinalized) {
            changeSet.add(graph.getTopologicalOrder(node.graphNodeId()));
        }
    }

    @Override
    public void updateChanged() {
        BitSet visited;
        if (!changeSet.isEmpty()) {
            visited = new BitSet(instanceList.size());
            visited.set(changeSet.peek().nodeId());
        } else {
            return;
        }

        // NOTE: This assumes the user did not add any fixed loops to
        // their graph (i.e. have two variables ALWAYS depend on one-another).
        while (!changeSet.isEmpty()) {
            var changedNode = changeSet.poll();
            var entityVariable = instanceList.get(changedNode.nodeId());
            var entity = entityVariable.entity();
            var shadowVariableReferences = entityVariable.variableReferences();
            for (var shadowVariableReference : shadowVariableReferences) {
                var isVariableChanged = shadowVariableReference.updateIfChanged(entity, changedVariableNotifier);
                if (isVariableChanged) {
                    for (var iterator = graph.nodeForwardEdges(changedNode.nodeId()); iterator.hasNext();) {
                        var nextNode = iterator.next();
                        if (visited.get(nextNode)) {
                            continue;
                        }
                        visited.set(nextNode);
                        changeSet.add(graph.getTopologicalOrder(nextNode));
                    }
                }
            }
        }
    }
}
