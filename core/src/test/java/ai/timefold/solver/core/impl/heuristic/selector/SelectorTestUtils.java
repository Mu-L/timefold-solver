package ai.timefold.solver.core.impl.heuristic.selector;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.config.heuristic.selector.common.SelectionCacheType;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.entity.EntitySelector;
import ai.timefold.solver.core.impl.heuristic.selector.entity.mimic.MimicReplayingEntitySelector;
import ai.timefold.solver.core.impl.heuristic.selector.list.SubList;
import ai.timefold.solver.core.impl.heuristic.selector.list.mimic.MimicReplayingSubListSelector;
import ai.timefold.solver.core.impl.heuristic.selector.move.MoveSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.ValueSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.mimic.MimicReplayingValueSelector;
import ai.timefold.solver.core.impl.phase.event.PhaseLifecycleListener;
import ai.timefold.solver.core.impl.phase.scope.AbstractPhaseScope;
import ai.timefold.solver.core.impl.phase.scope.AbstractStepScope;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.solver.scope.SolverScope;
import ai.timefold.solver.core.testdomain.chained.TestdataChainedEntity;
import ai.timefold.solver.core.testdomain.chained.TestdataChainedObject;

public class SelectorTestUtils {

    public static <Solution_> EntityDescriptor<Solution_> mockEntityDescriptor(Class<?> entityClass) {
        EntityDescriptor<Solution_> entityDescriptor = mock(EntityDescriptor.class);
        when(entityDescriptor.getEntityClass()).thenReturn((Class) entityClass);
        return entityDescriptor;
    }

    public static <Solution_> GenuineVariableDescriptor<Solution_> mockVariableDescriptor(Class<?> entityClass,
            String variableName) {
        EntityDescriptor<Solution_> entityDescriptor = mockEntityDescriptor(entityClass);
        return mockVariableDescriptor(entityDescriptor, variableName);
    }

    public static <Solution_> GenuineVariableDescriptor<Solution_> mockVariableDescriptor(
            EntityDescriptor<Solution_> entityDescriptor, String variableName) {
        GenuineVariableDescriptor<Solution_> variableDescriptor = mock(GenuineVariableDescriptor.class);
        when(variableDescriptor.getEntityDescriptor()).thenReturn(entityDescriptor);
        when(variableDescriptor.getVariableName()).thenReturn(variableName);
        return variableDescriptor;
    }

    public static <Solution_> EntitySelector<Solution_> mockEntitySelector(Class<?> entityClass, Object... entities) {
        EntityDescriptor<Solution_> entityDescriptor = mockEntityDescriptor(entityClass);
        return mockEntitySelector(entityDescriptor, entities);
    }

    public static <Solution_> EntitySelector<Solution_> mockEntitySelector(EntityDescriptor<Solution_> entityDescriptor,
            Object... entities) {
        EntitySelector<Solution_> entitySelector = mock(EntitySelector.class);
        when(entitySelector.getEntityDescriptor()).thenReturn(entityDescriptor);
        final List<Object> entityList = Arrays.asList(entities);
        when(entitySelector.iterator()).thenAnswer(invocation -> entityList.iterator());
        when(entitySelector.listIterator()).thenAnswer(invocation -> entityList.listIterator());
        when(entitySelector.spliterator()).thenAnswer(invocation -> entityList.spliterator());
        for (int i = 0; i < entityList.size(); i++) {
            final int index = i;
            when(entitySelector.listIterator(index)).thenAnswer(invocation -> entityList.listIterator(index));
        }
        when(entitySelector.endingIterator()).thenAnswer(invocation -> entityList.iterator());
        when(entitySelector.isCountable()).thenReturn(true);
        when(entitySelector.isNeverEnding()).thenReturn(false);
        when(entitySelector.getSize()).thenReturn((long) entityList.size());
        return entitySelector;
    }

    public static <Solution_> ValueSelector<Solution_> mockValueSelector(Class<?> entityClass, String variableName,
            Object... values) {
        GenuineVariableDescriptor<Solution_> variableDescriptor = mockVariableDescriptor(entityClass, variableName);
        return mockValueSelector(variableDescriptor, values);
    }

    public static <Solution_> ValueSelector<Solution_> mockValueSelector(
            GenuineVariableDescriptor<Solution_> variableDescriptor, Object... values) {
        ValueSelector<Solution_> valueSelector = mock(ValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(variableDescriptor);
        final List<Object> valueList = Arrays.asList(values);
        when(valueSelector.iterator(any())).thenAnswer(invocation -> valueList.iterator());
        when(valueSelector.isCountable()).thenReturn(true);
        when(valueSelector.isNeverEnding()).thenReturn(false);
        when(valueSelector.getSize(any())).thenReturn((long) valueList.size());
        return valueSelector;
    }

    public static <Solution_> ValueSelector<Solution_> mockValueSelectorForEntity(Class<?> entityClass, Object entity,
            String variableName, Object... values) {
        return mockValueSelectorForEntity(entityClass, variableName,
                Collections.singletonMap(entity, Arrays.asList(values)));
    }

    private static <Solution_> ValueSelector<Solution_> mockValueSelectorForEntity(Class<?> entityClass,
            String variableName, Map<Object, List<Object>> entityToValues) {
        GenuineVariableDescriptor<Solution_> variableDescriptor = mockVariableDescriptor(entityClass, variableName);
        return mockValueSelectorForEntity(variableDescriptor, entityToValues);
    }

    private static <Solution_> ValueSelector<Solution_> mockValueSelectorForEntity(
            GenuineVariableDescriptor<Solution_> variableDescriptor, Map<Object, List<Object>> entityToValues) {
        ValueSelector<Solution_> valueSelector = mock(ValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(variableDescriptor);
        for (Map.Entry<Object, List<Object>> entry : entityToValues.entrySet()) {
            Object entity = entry.getKey();
            final List<Object> valueList = entry.getValue();
            when(valueSelector.getSize(entity)).thenAnswer(invocation -> (long) valueList.size());
            when(valueSelector.iterator(entity)).thenAnswer(invocation -> valueList.iterator());
            when(valueSelector.getSize(entity)).thenReturn((long) valueList.size());
        }
        when(valueSelector.isCountable()).thenReturn(true);
        when(valueSelector.isNeverEnding()).thenReturn(false);
        return valueSelector;
    }

    public static <Solution_> IterableValueSelector<Solution_> mockIterableValueSelector(
            Class<?> entityClass, String variableName, Object... values) {
        GenuineVariableDescriptor<Solution_> variableDescriptor = mockVariableDescriptor(entityClass, variableName);
        when(variableDescriptor.canExtractValueRangeFromSolution()).thenReturn(true);
        return mockIterableValueSelector(variableDescriptor, values);
    }

    public static <Solution_> IterableValueSelector<Solution_> mockIterableValueSelector(
            GenuineVariableDescriptor<Solution_> variableDescriptor, Object... values) {
        IterableValueSelector<Solution_> valueSelector = mock(IterableValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(variableDescriptor);
        final List<Object> valueList = Arrays.asList(values);
        when(valueSelector.iterator(any())).thenAnswer(invocation -> valueList.iterator());
        when(valueSelector.endingIterator(any())).thenAnswer(invocation -> valueList.iterator());
        when(valueSelector.iterator()).thenAnswer(invocation -> valueList.iterator());
        when(valueSelector.spliterator()).thenAnswer(invocation -> valueList.spliterator());
        when(valueSelector.isCountable()).thenReturn(true);
        when(valueSelector.isNeverEnding()).thenReturn(false);
        when(valueSelector.getSize(any())).thenReturn((long) valueList.size());
        when(valueSelector.getSize()).thenReturn((long) valueList.size());
        return valueSelector;
    }

    public static <Solution_> MimicReplayingEntitySelector<Solution_> mockReplayingEntitySelector(
            EntityDescriptor<Solution_> entityDescriptor, Object... entities) {
        MimicReplayingEntitySelector<Solution_> entitySelector = mock(MimicReplayingEntitySelector.class);
        when(entitySelector.getEntityDescriptor()).thenReturn(entityDescriptor);
        final List<Object> entityList = Arrays.asList(entities);
        when(entitySelector.endingIterator()).thenAnswer(invocation -> entityList.iterator());
        when(entitySelector.iterator()).thenAnswer(invocation -> entityList.iterator());
        return entitySelector;
    }

    public static <Solution_> MimicReplayingValueSelector<Solution_> mockReplayingValueSelector(
            GenuineVariableDescriptor<Solution_> variableDescriptor, Object... values) {
        MimicReplayingValueSelector<Solution_> valueSelector = mock(MimicReplayingValueSelector.class);
        when(valueSelector.getVariableDescriptor()).thenReturn(variableDescriptor);
        final List<Object> valueList = Arrays.asList(values);
        when(valueSelector.endingIterator(any())).thenAnswer(invocation -> valueList.iterator());
        when(valueSelector.iterator()).thenAnswer(invocation -> valueList.iterator());
        return valueSelector;
    }

    public static <Solution_> MimicReplayingSubListSelector<Solution_> mockReplayingSubListSelector(
            ListVariableDescriptor<Solution_> variableDescriptor, SubList... subLists) {
        MimicReplayingSubListSelector<Solution_> subListSelector = mock(MimicReplayingSubListSelector.class);
        when(subListSelector.getVariableDescriptor()).thenReturn(variableDescriptor);
        final List<SubList> subListList = Arrays.asList(subLists);
        when(subListSelector.iterator()).thenAnswer(invocation -> subListList.iterator());
        List<Object> distinctValueList = subListList.stream()
                .map(subList -> variableDescriptor.getElement(subList.entity(), subList.fromIndex()))
                .distinct()
                .toList();
        when(subListSelector.endingValueIterator()).thenAnswer(invocation -> distinctValueList.iterator());
        return subListSelector;
    }

    @SafeVarargs
    public static <Solution_> MoveSelector<Solution_> mockMoveSelector(Move<Solution_>... moves) {
        MoveSelector<Solution_> moveSelector = mock(MoveSelector.class);
        final List<Move<Solution_>> moveList = Arrays.asList(moves);
        when(moveSelector.iterator()).thenAnswer(invocation -> moveList.iterator());
        when(moveSelector.spliterator()).thenAnswer(invocation -> moveList.spliterator());
        when(moveSelector.isCountable()).thenReturn(true);
        when(moveSelector.isNeverEnding()).thenReturn(false);
        when(moveSelector.getCacheType()).thenReturn(SelectionCacheType.JUST_IN_TIME);
        when(moveSelector.getSize()).thenReturn((long) moveList.size());
        when(moveSelector.supportsPhaseAndSolverCaching()).thenReturn(true);
        return moveSelector;
    }

    public static SingletonInverseVariableSupply mockSingletonInverseVariableSupply(
            final TestdataChainedEntity[] allEntities) {
        return planningValue -> {
            for (TestdataChainedEntity entity : allEntities) {
                if (entity.getChainedObject().equals(planningValue)) {
                    return entity;
                }
            }
            return null;
        };
    }

    public static void assertChain(TestdataChainedObject... chainedObjects) {
        TestdataChainedObject chainedObject = chainedObjects[0];
        for (int i = 1; i < chainedObjects.length; i++) {
            TestdataChainedEntity chainedEntity = (TestdataChainedEntity) chainedObjects[i];
            if (!Objects.equals(chainedObject, chainedEntity.getChainedObject())) {
                fail("Chain assertion failed for chainedEntity (" + chainedEntity + ").\n"
                        + "Expected: " + chainedObject + "\n"
                        + "Actual:   " + chainedEntity.getChainedObject() + "\n"
                        + "Expected chain: " + Arrays.toString(chainedObjects) + "\n"
                        + "Actual chain:   " + Arrays.toString(Arrays.copyOf(chainedObjects, i)) + " ... ["
                        + chainedEntity.getChainedObject() + ", " + chainedEntity + "] ...");
            }
            chainedObject = chainedEntity;
        }
    }

    // ************************************************************************
    // Lifecycle
    // ************************************************************************

    public static <Solution_> SolverScope<Solution_> solvingStarted(PhaseLifecycleListener<Solution_> listener) {
        return solvingStarted(listener, null, null, null);
    }

    public static <Solution_, Score_ extends Score<Score_>> SolverScope<Solution_> solvingStarted(
            PhaseLifecycleListener<Solution_> listener, InnerScoreDirector<Solution_, Score_> scoreDirector) {
        return solvingStarted(listener, scoreDirector, null, null);
    }

    public static <Solution_, Score_ extends Score<Score_>> SolverScope<Solution_> solvingStarted(
            PhaseLifecycleListener<Solution_> listener, InnerScoreDirector<Solution_, Score_> scoreDirector,
            ValueSelector<Solution_> valueSelector) {
        return solvingStarted(listener, scoreDirector, null, valueSelector);
    }

    public static <Solution_, Score_ extends Score<Score_>> SolverScope<Solution_> solvingStarted(
            PhaseLifecycleListener<Solution_> listener, InnerScoreDirector<Solution_, Score_> scoreDirector,
            Random random) {
        return solvingStarted(listener, scoreDirector, random, null);
    }

    public static <Solution_, Score_ extends Score<Score_>> SolverScope<Solution_> solvingStarted(
            PhaseLifecycleListener<Solution_> listener, InnerScoreDirector<Solution_, Score_> scoreDirector, Random random,
            ValueSelector<Solution_> valueSelector) {
        SolverScope<Solution_> solverScope = new SolverScope<>();
        solverScope.setScoreDirector(scoreDirector);
        solverScope.setWorkingRandom(random);
        listener.solvingStarted(solverScope);
        if (valueSelector != null) {
            valueSelector.solvingStarted(solverScope);
        }
        return solverScope;
    }

    public static <Solution_> AbstractPhaseScope<Solution_> phaseStarted(PhaseLifecycleListener<Solution_> listener,
            SolverScope<Solution_> solverScope) {
        AbstractPhaseScope<Solution_> phaseScope = mock(AbstractPhaseScope.class);
        when(phaseScope.getSolverScope()).thenReturn(solverScope);
        when(phaseScope.getScoreDirector()).thenAnswer(s -> solverScope.getScoreDirector());
        listener.phaseStarted(phaseScope);
        return phaseScope;
    }

    public static <Solution_> AbstractStepScope<Solution_> stepStarted(PhaseLifecycleListener<Solution_> listener,
            AbstractPhaseScope<Solution_> phaseScope) {
        AbstractStepScope<Solution_> stepScope = mock(AbstractStepScope.class);
        when(stepScope.getPhaseScope()).thenReturn(phaseScope);
        listener.stepStarted(stepScope);
        return stepScope;
    }

    public static <Solution_, T extends PhaseLifecycleListener<Solution_>> void doInsideStep(T selector,
            AbstractPhaseScope<Solution_> phaseScope, Consumer<T> selectorAction) {
        AbstractStepScope<Solution_> stepScope = stepStarted(selector, phaseScope);
        selectorAction.accept(selector);
        selector.stepEnded(stepScope);
    }

    private SelectorTestUtils() {
    }

}
