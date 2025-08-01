package ai.timefold.solver.core.impl.heuristic.selector.list;

import java.util.Objects;

import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionOrder;
import ai.timefold.solver.core.config.heuristic.selector.common.nearby.NearbySelectionConfig;
import ai.timefold.solver.core.config.heuristic.selector.list.DestinationSelectorConfig;
import ai.timefold.solver.core.enterprise.TimefoldSolverEnterpriseService;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.heuristic.selector.AbstractSelectorFactory;
import ai.timefold.solver.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelectorFactory;

public final class DestinationSelectorFactory<Solution_> extends AbstractSelectorFactory<Solution_, DestinationSelectorConfig> {

    public static <Solution_> DestinationSelectorFactory<Solution_>
            create(DestinationSelectorConfig destinationSelectorConfig) {
        return new DestinationSelectorFactory<>(destinationSelectorConfig);
    }

    private DestinationSelectorFactory(DestinationSelectorConfig destinationSelectorConfig) {
        super(destinationSelectorConfig);
    }

    public DestinationSelector<Solution_> buildDestinationSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        var selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        var entitySelector = EntitySelectorFactory.<Solution_> create(Objects.requireNonNull(config.getEntitySelectorConfig()))
                .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);
        var valueSelector = buildIterableValueSelector(configPolicy, entitySelector.getEntityDescriptor(),
                minimumCacheType, selectionOrder);
        var baseDestinationSelector =
                new ElementDestinationSelector<>(entitySelector, valueSelector, selectionOrder.toRandomSelectionBoolean());
        return applyNearbySelection(configPolicy, minimumCacheType, selectionOrder, baseDestinationSelector);
    }

    private IterableValueSelector<Solution_> buildIterableValueSelector(
            HeuristicConfigPolicy<Solution_> configPolicy, EntityDescriptor<Solution_> entityDescriptor,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder) {
        ValueSelector<Solution_> valueSelector = ValueSelectorFactory
                .<Solution_> create(Objects.requireNonNull(config.getValueSelectorConfig()))
                .buildValueSelector(configPolicy, entityDescriptor, minimumCacheType, inheritedSelectionOrder,
                        // Do not override reinitializeVariableFilterEnabled.
                        configPolicy.isReinitializeVariableFilterEnabled(),
                        /*
                         * Filter assigned values (but only if this filtering type is allowed by the configPolicy).
                         *
                         * The destination selector requires the child value selector to only select assigned values.
                         * To guarantee this during CH, where not all values are assigned, the UnassignedValueSelector filter
                         * must be applied.
                         *
                         * In the LS phase, not only is the filter redundant because there are no unassigned values,
                         * but it would also crash if the base value selector inherits random selection order,
                         * because the filter cannot work on a never-ending child value selector.
                         * Therefore, it must not be applied even though it is requested here. This is accomplished by
                         * the configPolicy that only allows this filtering type in the CH phase.
                         */
                        ValueSelectorFactory.ListValueFilteringType.ACCEPT_ASSIGNED);
        return (IterableValueSelector<Solution_>) valueSelector;
    }

    private DestinationSelector<Solution_> applyNearbySelection(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, SelectionOrder resolvedSelectionOrder,
            ElementDestinationSelector<Solution_> destinationSelector) {
        NearbySelectionConfig nearbySelectionConfig = config.getNearbySelectionConfig();
        if (nearbySelectionConfig == null) {
            return destinationSelector;
        }
        return TimefoldSolverEnterpriseService.loadOrFail(TimefoldSolverEnterpriseService.Feature.NEARBY_SELECTION)
                .applyNearbySelection(config, configPolicy, minimumCacheType, resolvedSelectionOrder, destinationSelector);
    }
}
