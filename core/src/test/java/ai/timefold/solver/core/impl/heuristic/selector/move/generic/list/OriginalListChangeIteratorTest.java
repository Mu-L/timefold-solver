package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list;

import static ai.timefold.solver.core.testdomain.list.TestdataListUtils.getListVariableDescriptor;
import static ai.timefold.solver.core.testdomain.list.TestdataListUtils.mockEntitySelector;
import static ai.timefold.solver.core.testdomain.list.TestdataListUtils.mockIterableValueSelector;
import static ai.timefold.solver.core.testutil.PlannerTestUtils.mockScoreDirector;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.heuristic.selector.list.ElementDestinationSelector;
import ai.timefold.solver.core.impl.heuristic.selector.value.IterableValueSelector;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.testdomain.list.TestdataListEntity;
import ai.timefold.solver.core.testdomain.list.TestdataListSolution;
import ai.timefold.solver.core.testdomain.list.TestdataListValue;

import org.junit.jupiter.api.Test;

class OriginalListChangeIteratorTest {

    @Test
    void emptyValueSelector() {
        assertEmptyIterator(emptyList(), singletonList(new TestdataListEntity("e1")));
    }

    @Test
    void emptyEntitySelector() {
        assertEmptyIterator(singletonList(new TestdataListValue("v1")), emptyList());
    }

    static void assertEmptyIterator(List<Object> values, List<TestdataListEntity> entities) {
        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector =
                mockScoreDirector(TestdataListSolution.buildSolutionDescriptor());
        ListVariableDescriptor<TestdataListSolution> listVariableDescriptor = getListVariableDescriptor(scoreDirector);
        IterableValueSelector<TestdataListSolution> valueSelector =
                mockIterableValueSelector(listVariableDescriptor, values.toArray());
        OriginalListChangeIterator<TestdataListSolution> listChangeIterator = new OriginalListChangeIterator<>(
                scoreDirector.getSupplyManager().demand(listVariableDescriptor.getStateDemand()),
                valueSelector,
                new ElementDestinationSelector<>(
                        mockEntitySelector(entities.toArray(TestdataListEntity[]::new)),
                        valueSelector,
                        false));

        assertThat(listChangeIterator).isExhausted();
    }
}
