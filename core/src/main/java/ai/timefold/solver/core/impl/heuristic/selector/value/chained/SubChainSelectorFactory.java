package ai.timefold.solver.core.impl.heuristic.selector.value.chained;

import java.util.Objects;

import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionOrder;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.ValueSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelectorFactory;

public class SubChainSelectorFactory<Solution_> {

    /**
     * Defaults to 1, even if it partially duplicates {@link ChangeMoveSelectorConfig},
     * because otherwise the default would not include
     * swapping a pillar of size 1 with another pillar of size 2 or greater.
     */
    private static final int DEFAULT_MINIMUM_SUB_CHAIN_SIZE = 1;
    private static final int DEFAULT_MAXIMUM_SUB_CHAIN_SIZE = Integer.MAX_VALUE;

    public static <Solution_> SubChainSelectorFactory<Solution_> create(SubChainSelectorConfig subChainSelectorConfig) {
        return new SubChainSelectorFactory<>(subChainSelectorConfig);
    }

    private final SubChainSelectorConfig config;

    public SubChainSelectorFactory(SubChainSelectorConfig subChainSelectorConfig) {
        this.config = subChainSelectorConfig;
    }

    /**
     * @param configPolicy never null
     * @param entityDescriptor never null
     * @param minimumCacheType never null, If caching is used (different from {@link SelectionCacheType#JUST_IN_TIME}),
     *        then it should be at least this {@link SelectionCacheType} because an ancestor already uses such caching
     *        and less would be pointless.
     * @param inheritedSelectionOrder never null
     * @return never null
     */
    public SubChainSelector<Solution_> buildSubChainSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            EntityDescriptor<Solution_> entityDescriptor, SelectionCacheType minimumCacheType,
            SelectionOrder inheritedSelectionOrder) {
        if (minimumCacheType.compareTo(SelectionCacheType.STEP) > 0) {
            throw new IllegalArgumentException("The subChainSelectorConfig (" + config
                    + ")'s minimumCacheType (" + minimumCacheType
                    + ") must not be higher than " + SelectionCacheType.STEP
                    + " because the chains change every step.");
        }
        ValueSelectorConfig valueSelectorConfig =
                Objects.requireNonNullElseGet(config.getValueSelectorConfig(), ValueSelectorConfig::new);
        // ValueSelector uses SelectionOrder.ORIGINAL because a SubChainSelector STEP caches the values
        ValueSelector<Solution_> valueSelector = ValueSelectorFactory.<Solution_> create(valueSelectorConfig)
                .buildValueSelector(configPolicy, entityDescriptor, minimumCacheType, SelectionOrder.ORIGINAL);
        return new DefaultSubChainSelector<>((IterableValueSelector<Solution_>) valueSelector,
                inheritedSelectionOrder.toRandomSelectionBoolean(),
                Objects.requireNonNullElse(config.getMinimumSubChainSize(), DEFAULT_MINIMUM_SUB_CHAIN_SIZE),
                Objects.requireNonNullElse(config.getMaximumSubChainSize(), DEFAULT_MAXIMUM_SUB_CHAIN_SIZE));
    }
}
