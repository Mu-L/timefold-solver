package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionOrder;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.list.kopt.KOptListMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.ValueSelectorConfig;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import ai.timefold.solver.core.impl.heuristic.selector.move.MoveSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelectorFactory;

public final class KOptListMoveSelectorFactory<Solution_>
        extends AbstractMoveSelectorFactory<Solution_, KOptListMoveSelectorConfig> {

    private static final int DEFAULT_MINIMUM_K = 2;
    private static final int DEFAULT_MAXIMUM_K = 2;

    public KOptListMoveSelectorFactory(KOptListMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    protected MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        var listVariableDescriptor = configPolicy.getSolutionDescriptor().getListVariableDescriptor();
        if (listVariableDescriptor == null) {
            throw new IllegalArgumentException("""
                    The kOptListMoveSelector (%s) can only be used when the domain model has a list variable.
                    Check your @%s and make sure it has a @%s."""
                    .formatted(config, PlanningEntity.class.getSimpleName(), PlanningListVariable.class.getSimpleName()));
        }

        var originSelectorConfig = Objects.requireNonNullElseGet(config.getOriginSelectorConfig(), ValueSelectorConfig::new);
        var valueSelectorConfig = Objects.requireNonNullElseGet(config.getValueSelectorConfig(), ValueSelectorConfig::new);

        var entityDescriptor = getTheOnlyEntityDescriptorWithListVariable(configPolicy.getSolutionDescriptor());
        if (originSelectorConfig.getVariableName() == null) {
            originSelectorConfig.setVariableName(listVariableDescriptor.getVariableName());
        }
        if (valueSelectorConfig.getVariableName() == null) {
            valueSelectorConfig.setVariableName(listVariableDescriptor.getVariableName());
        }
        var selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        var originSelector = buildIterableValueSelector(configPolicy, entityDescriptor, originSelectorConfig,
                minimumCacheType, selectionOrder);
        var valueSelector = buildIterableValueSelector(configPolicy, entityDescriptor, valueSelectorConfig,
                minimumCacheType, selectionOrder);

        int minimumK = Objects.requireNonNullElse(config.getMinimumK(), DEFAULT_MINIMUM_K);
        if (minimumK < 2) {
            throw new IllegalArgumentException("minimumK (%d) must be at least 2."
                    .formatted(minimumK));
        }
        int maximumK = Objects.requireNonNullElse(config.getMaximumK(), DEFAULT_MAXIMUM_K);
        if (maximumK < minimumK) {
            throw new IllegalArgumentException("maximumK (%d) must be at least minimumK (%d)."
                    .formatted(maximumK, minimumK));
        }

        var pickedKDistribution = new int[maximumK - minimumK + 1];
        // Each prior k is 8 times more likely to be picked than the subsequent k
        var total = 1;
        for (var i = minimumK; i < maximumK; i++) {
            total *= 8;
        }
        for (var i = 0; i < pickedKDistribution.length - 1; i++) {
            int remainder = total / 8;
            pickedKDistribution[i] = total - remainder;
            total = remainder;
        }
        pickedKDistribution[pickedKDistribution.length - 1] = total;
        return new KOptListMoveSelector<>(listVariableDescriptor, originSelector, valueSelector, minimumK, maximumK,
                pickedKDistribution);
    }

    private IterableValueSelector<Solution_> buildIterableValueSelector(
            HeuristicConfigPolicy<Solution_> configPolicy,
            EntityDescriptor<Solution_> entityDescriptor,
            ValueSelectorConfig valueSelectorConfig,
            SelectionCacheType minimumCacheType,
            SelectionOrder inheritedSelectionOrder) {
        var valueSelector = ValueSelectorFactory.<Solution_> create(valueSelectorConfig)
                .buildValueSelector(configPolicy, entityDescriptor, minimumCacheType, inheritedSelectionOrder);
        if (valueSelector instanceof IterableValueSelector<Solution_> iterableValueSelector) {
            return iterableValueSelector;
        }
        throw new IllegalArgumentException("""
                The kOptListMoveSelector (%s) for a list variable needs to be based on an %s (%s).
                Check your valueSelectorConfig."""
                .formatted(config, IterableValueSelector.class.getSimpleName(), valueSelector));
    }
}
