package ai.timefold.solver.core.impl.heuristic.selector.value.decorator;

import static ai.timefold.solver.core.testutil.PlannerAssert.assertAllCodesOfValueSelectorForEntity;
import static ai.timefold.solver.core.testutil.PlannerAssert.verifyPhaseLifecycle;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.SelectorTestUtils;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.phase.scope.AbstractStepScope;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.testdomain.TestdataEntity;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testdomain.multivar.TestdataMultiVarEntity;

import org.junit.jupiter.api.Test;

class ReinitializeVariableValueSelectorTest {

    @Test
    void oneVariable() {
        EntityDescriptor entityDescriptor = TestdataEntity.buildEntityDescriptor();
        GenuineVariableDescriptor variableDescriptor = entityDescriptor.getGenuineVariableDescriptor("value");
        TestdataEntity e1 = new TestdataEntity("e1");
        TestdataEntity e2 = new TestdataEntity("e2");
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        ValueSelector childValueSelector = SelectorTestUtils.mockValueSelector(variableDescriptor,
                v1, v2, v3);

        ValueSelector valueSelector = new ReinitializeVariableValueSelector(childValueSelector);

        SolverScope solverScope = mock(SolverScope.class);
        valueSelector.solvingStarted(solverScope);

        AbstractPhaseScope phaseScopeA = mock(AbstractPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA1);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e1, "v1", "v2", "v3");
        valueSelector.stepEnded(stepScopeA1);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA2);
        e2.setValue(v2);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e2);
        valueSelector.stepEnded(stepScopeA2);

        valueSelector.phaseEnded(phaseScopeA);

        AbstractPhaseScope phaseScopeB = mock(AbstractPhaseScope.class);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB1);
        e2.setValue(null);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e2, "v1", "v2", "v3");
        valueSelector.stepEnded(stepScopeB1);

        AbstractStepScope stepScopeB2 = mock(AbstractStepScope.class);
        when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB2);
        e1.setValue(v3);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e1);
        valueSelector.stepEnded(stepScopeB2);

        valueSelector.phaseEnded(phaseScopeB);

        valueSelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childValueSelector, 1, 2, 4);
        verify(childValueSelector, atMost(4)).iterator(any());
        verify(childValueSelector, atMost(4)).getSize(any());
    }

    @Test
    void multiVariable() {
        EntityDescriptor entityDescriptor = TestdataMultiVarEntity.buildEntityDescriptor();
        GenuineVariableDescriptor variableDescriptor = entityDescriptor.getGenuineVariableDescriptor("secondaryValue");
        TestdataMultiVarEntity e1 = new TestdataMultiVarEntity("e1");
        TestdataMultiVarEntity e2 = new TestdataMultiVarEntity("e2");
        TestdataValue p1 = new TestdataValue("p1");
        TestdataValue s1 = new TestdataValue("s1");
        TestdataValue s2 = new TestdataValue("s2");
        TestdataValue s3 = new TestdataValue("s3");
        ValueSelector childValueSelector = SelectorTestUtils.mockValueSelector(variableDescriptor,
                s1, s2, s3);

        ValueSelector valueSelector = new ReinitializeVariableValueSelector(childValueSelector);

        SolverScope solverScope = mock(SolverScope.class);
        valueSelector.solvingStarted(solverScope);

        AbstractPhaseScope phaseScopeA = mock(AbstractPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeA);

        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA1);
        e1.setPrimaryValue(p1);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e1, "s1", "s2", "s3");
        valueSelector.stepEnded(stepScopeA1);

        AbstractStepScope stepScopeA2 = mock(AbstractStepScope.class);
        when(stepScopeA2.getPhaseScope()).thenReturn(phaseScopeA);
        valueSelector.stepStarted(stepScopeA2);
        e2.setSecondaryValue(s2);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e2);
        valueSelector.stepEnded(stepScopeA2);

        valueSelector.phaseEnded(phaseScopeA);

        AbstractPhaseScope phaseScopeB = mock(AbstractPhaseScope.class);
        when(phaseScopeB.getSolverScope()).thenReturn(solverScope);
        valueSelector.phaseStarted(phaseScopeB);

        AbstractStepScope stepScopeB1 = mock(AbstractStepScope.class);
        when(stepScopeB1.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB1);
        e2.setSecondaryValue(null);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e2, "s1", "s2", "s3");
        valueSelector.stepEnded(stepScopeB1);

        AbstractStepScope stepScopeB2 = mock(AbstractStepScope.class);
        when(stepScopeB2.getPhaseScope()).thenReturn(phaseScopeB);
        valueSelector.stepStarted(stepScopeB2);
        e1.setPrimaryValue(null);
        assertAllCodesOfValueSelectorForEntity(valueSelector, e1, "s1", "s2", "s3");
        valueSelector.stepEnded(stepScopeB2);

        valueSelector.phaseEnded(phaseScopeB);

        valueSelector.solvingEnded(solverScope);

        verifyPhaseLifecycle(childValueSelector, 1, 2, 4);
        verify(childValueSelector, atMost(4)).iterator(any());
        verify(childValueSelector, atMost(4)).getSize(any());
    }

}
