package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

import ai.timefold.solver.core.impl.domain.variable.ListVariableStateSupply;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.list.DestinationSelector;
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.GenericMoveSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.decorator.FilteringValueSelector;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.preview.api.domain.metamodel.UnassignedElement;

public class ListChangeMoveSelector<Solution_> extends GenericMoveSelector<Solution_> {

    private final IterableValueSelector<Solution_> sourceValueSelector;
    private final DestinationSelector<Solution_> destinationSelector;
    private final boolean randomSelection;

    private ListVariableStateSupply<Solution_> listVariableStateSupply;

    public ListChangeMoveSelector(IterableValueSelector<Solution_> sourceValueSelector,
            DestinationSelector<Solution_> destinationSelector, boolean randomSelection) {
        this.sourceValueSelector =
                filterPinnedListPlanningVariableValuesWithIndex(sourceValueSelector, this::getListVariableStateSupply);
        this.destinationSelector = destinationSelector;
        this.randomSelection = randomSelection;
        phaseLifecycleSupport.addEventListener(this.sourceValueSelector);
        phaseLifecycleSupport.addEventListener(this.destinationSelector);
    }

    private ListVariableStateSupply<Solution_> getListVariableStateSupply() {
        return Objects.requireNonNull(listVariableStateSupply,
                "Impossible state: The listVariableStateSupply is not initialized yet.");
    }

    @Override
    public void solvingStarted(SolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        var listVariableDescriptor = (ListVariableDescriptor<Solution_>) sourceValueSelector.getVariableDescriptor();
        var supplyManager = solverScope.getScoreDirector().getSupplyManager();
        this.listVariableStateSupply = supplyManager.demand(listVariableDescriptor.getStateDemand());
    }

    public static <Solution_> IterableValueSelector<Solution_> filterPinnedListPlanningVariableValuesWithIndex(
            IterableValueSelector<Solution_> sourceValueSelector,
            Supplier<ListVariableStateSupply<Solution_>> listVariableStateSupplier) {
        var listVariableDescriptor = (ListVariableDescriptor<Solution_>) sourceValueSelector.getVariableDescriptor();
        var supportsPinning = listVariableDescriptor.supportsPinning();
        if (!supportsPinning) {
            // Don't incur the overhead of filtering values if there is no pinning support.
            return sourceValueSelector;
        }
        return (IterableValueSelector<Solution_>) FilteringValueSelector.of(sourceValueSelector,
                (scoreDirector, selection) -> {
                    var listVariableStateSupply = listVariableStateSupplier.get();
                    var elementPosition = listVariableStateSupply.getElementPosition(selection);
                    if (elementPosition instanceof UnassignedElement) {
                        return true;
                    }
                    var elementDestination = elementPosition.ensureAssigned();
                    var entity = elementDestination.entity();
                    return !listVariableDescriptor.isElementPinned(scoreDirector.getWorkingSolution(), entity,
                            elementDestination.index());
                });
    }

    @Override
    public void solvingEnded(SolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        listVariableStateSupply = null;
    }

    @Override
    public long getSize() {
        return sourceValueSelector.getSize() * destinationSelector.getSize();
    }

    @Override
    public Iterator<Move<Solution_>> iterator() {
        if (randomSelection) {
            return new RandomListChangeIterator<>(
                    listVariableStateSupply,
                    sourceValueSelector,
                    destinationSelector);
        } else {
            return new OriginalListChangeIterator<>(
                    listVariableStateSupply,
                    sourceValueSelector,
                    destinationSelector);
        }
    }

    @Override
    public boolean isCountable() {
        return sourceValueSelector.isCountable() && destinationSelector.isCountable();
    }

    @Override
    public boolean isNeverEnding() {
        return randomSelection || sourceValueSelector.isNeverEnding() || destinationSelector.isNeverEnding();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceValueSelector + ", " + destinationSelector + ")";
    }
}
