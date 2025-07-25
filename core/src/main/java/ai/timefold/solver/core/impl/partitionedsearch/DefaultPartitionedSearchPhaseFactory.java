package ai.timefold.solver.core.impl.partitionedsearch;

import ai.timefold.solver.core.config.partitionedsearch.PartitionedSearchPhaseConfig;
import ai.timefold.solver.core.enterprise.TimefoldSolverEnterpriseService;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.phase.AbstractPhaseFactory;
import ai.timefold.solver.core.impl.score.director.ValueRangeManager;
import ai.timefold.solver.core.impl.solver.recaller.BestSolutionRecaller;
import ai.timefold.solver.core.impl.solver.termination.SolverTermination;

public class DefaultPartitionedSearchPhaseFactory<Solution_>
        extends AbstractPhaseFactory<Solution_, PartitionedSearchPhaseConfig> {

    public DefaultPartitionedSearchPhaseFactory(PartitionedSearchPhaseConfig phaseConfig) {
        super(phaseConfig);
    }

    @Override
    public PartitionedSearchPhase<Solution_> buildPhase(int phaseIndex, boolean lastInitializingPhase,
            HeuristicConfigPolicy<Solution_> solverConfigPolicy, BestSolutionRecaller<Solution_> bestSolutionRecaller,
            SolverTermination<Solution_> solverTermination, ValueRangeManager<Solution_> valueRangeManager) {
        return TimefoldSolverEnterpriseService.loadOrFail(TimefoldSolverEnterpriseService.Feature.PARTITIONED_SEARCH)
                .buildPartitionedSearch(phaseIndex, phaseConfig, solverConfigPolicy, solverTermination,
                        this::buildPhaseTermination);
    }

}
