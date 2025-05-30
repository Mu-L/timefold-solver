package ai.timefold.solver.core.impl.phase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.util.ArrayList;
import java.util.Arrays;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.phase.event.PhaseLifecycleListener;
import ai.timefold.solver.core.impl.solver.DefaultSolver;
import ai.timefold.solver.core.testdomain.TestdataEntity;
import ai.timefold.solver.core.testdomain.TestdataSolution;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testutil.PlannerAssert;
import ai.timefold.solver.core.testutil.PlannerTestUtils;

import org.junit.jupiter.api.Test;

class PhaseLifecycleTest {

    @Test
    void verifyEventCounts() {
        PhaseLifecycleListener listener = mock(PhaseLifecycleListener.class);
        // prepare solver
        SolverConfig solverConfig = PlannerTestUtils.buildSolverConfig(
                TestdataSolution.class, TestdataEntity.class);
        SolverFactory<TestdataSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<TestdataSolution> solver = solverFactory.buildSolver();

        // prepare solution
        TestdataSolution solution = new TestdataSolution("s1");
        solution.setValueList(Arrays.asList(new TestdataValue("v1"), new TestdataValue("v2")));
        final int entitiesCount = 17;
        ArrayList<TestdataEntity> entities = new ArrayList<>(entitiesCount);
        for (int i = 0; i < entitiesCount; i++) {
            entities.add(new TestdataEntity(String.valueOf(i)));
        }
        solution.setEntityList(entities);

        // add listener mock and solve
        ((DefaultSolver<TestdataSolution>) solver).addPhaseLifecycleListener(listener);
        TestdataSolution solvedSolution = solver.solve(solution);

        // step count = number of uninitialized entities (CH) + LS step count limit
        final int stepCount = entitiesCount + PlannerTestUtils.TERMINATION_STEP_COUNT_LIMIT;
        final int phaseCount = solverConfig.getPhaseConfigList().size();
        PlannerAssert.verifyPhaseLifecycle(listener, 1, phaseCount, stepCount);

        // forget previous invocations
        reset(listener);

        // uninitialize 1 entity and solve again
        solvedSolution.getEntityList().get(0).setValue(null);
        solver.solve(solvedSolution);
        PlannerAssert.verifyPhaseLifecycle(listener, 1, phaseCount, 1 + PlannerTestUtils.TERMINATION_STEP_COUNT_LIMIT);

        // forget previous invocations
        reset(listener);

        // remove listener and solve again
        ((DefaultSolver<TestdataSolution>) solver).removePhaseLifecycleListener(listener);
        solver.solve(solution);
        PlannerAssert.verifyPhaseLifecycle(listener, 0, 0, 0);
    }

}
