package ai.timefold.solver.core.impl.constructionheuristic.placer;

import static ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType.PHASE;
import static ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType.STEP;

import ai.timefold.solver.core.config.constructionheuristic.placer.EntityPlacerConfig;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionOrder;
import ai.timefold.solver.core.config.heuristic.selector.entity.EntitySelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.value.ValueSelectorConfig;
import ai.timefold.solver.core.impl.AbstractFromConfigFactory;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;

abstract class AbstractEntityPlacerFactory<Solution_, EntityPlacerConfig_ extends EntityPlacerConfig<EntityPlacerConfig_>>
        extends AbstractFromConfigFactory<Solution_, EntityPlacerConfig_> implements EntityPlacerFactory<Solution_> {

    protected AbstractEntityPlacerFactory(EntityPlacerConfig_ placerConfig) {
        super(placerConfig);
    }

    protected ChangeMoveSelectorConfig buildChangeMoveSelectorConfig(HeuristicConfigPolicy<Solution_> configPolicy,
            String entitySelectorConfigId, GenuineVariableDescriptor<Solution_> variableDescriptor) {
        ChangeMoveSelectorConfig changeMoveSelectorConfig = new ChangeMoveSelectorConfig();
        changeMoveSelectorConfig.setEntitySelectorConfig(
                EntitySelectorConfig.newMimicSelectorConfig(entitySelectorConfigId));
        ValueSelectorConfig changeValueSelectorConfig = new ValueSelectorConfig()
                .withVariableName(variableDescriptor.getVariableName());
        if (ValueSelectorConfig.hasSorter(configPolicy.getValueSorterManner(), variableDescriptor)) {
            changeValueSelectorConfig = changeValueSelectorConfig
                    .withCacheType(variableDescriptor.canExtractValueRangeFromSolution() ? PHASE : STEP)
                    .withSelectionOrder(SelectionOrder.SORTED)
                    .withSorterManner(configPolicy.getValueSorterManner());
        }
        return changeMoveSelectorConfig.withValueSelectorConfig(changeValueSelectorConfig);
    }
}
