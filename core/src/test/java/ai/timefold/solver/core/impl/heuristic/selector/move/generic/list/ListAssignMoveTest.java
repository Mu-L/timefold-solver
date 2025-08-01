package ai.timefold.solver.core.impl.heuristic.selector.move.generic.list;

import static ai.timefold.solver.core.testutil.PlannerTestUtils.mockRebasingScoreDirector;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import ai.timefold.solver.core.impl.move.director.MoveDirector;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.score.director.ValueRangeManager;
import ai.timefold.solver.core.testdomain.list.TestdataListEntity;
import ai.timefold.solver.core.testdomain.list.TestdataListSolution;
import ai.timefold.solver.core.testdomain.list.TestdataListValue;
import ai.timefold.solver.core.testdomain.list.valuerange.TestdataListEntityProvidingEntity;
import ai.timefold.solver.core.testdomain.list.valuerange.TestdataListEntityProvidingSolution;
import ai.timefold.solver.core.testdomain.list.valuerange.TestdataListEntityProvidingValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ListAssignMoveTest {

    private final InnerScoreDirector<TestdataListSolution, ?> innerScoreDirector = mock(InnerScoreDirector.class);
    private final MoveDirector<TestdataListSolution, ?> moveDirector = new MoveDirector<>(innerScoreDirector);
    private final ListVariableDescriptor<TestdataListSolution> variableDescriptor =
            TestdataListEntity.buildVariableDescriptorForValueList();
    private final InnerScoreDirector<TestdataListEntityProvidingSolution, ?> otherInnerScoreDirector =
            mock(InnerScoreDirector.class);
    private final ListVariableDescriptor<TestdataListEntityProvidingSolution> otherVariableDescriptor =
            TestdataListEntityProvidingEntity.buildVariableDescriptorForValueList();

    @BeforeEach
    void setUp() {
        when(innerScoreDirector.getSolutionDescriptor())
                .thenReturn(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        when(otherInnerScoreDirector.getValueRangeManager())
                .thenReturn(new ValueRangeManager<>(otherVariableDescriptor.getEntityDescriptor().getSolutionDescriptor()));
    }

    @Test
    void doMove() {
        var v1 = new TestdataListValue("1");
        var v2 = new TestdataListValue("2");
        var v3 = new TestdataListValue("3");
        var e1 = new TestdataListEntity("e1");

        moveDirector.executeTemporary(new ListAssignMove<>(variableDescriptor, v1, e1, 0),
                (__, ___) -> {
                    assertThat(e1.getValueList()).containsExactly(v1);
                    verify(innerScoreDirector).beforeListVariableChanged(variableDescriptor, e1, 0, 0);
                    verify(innerScoreDirector).beforeListVariableElementAssigned(variableDescriptor, v1);
                    verify(innerScoreDirector).afterListVariableElementAssigned(variableDescriptor, v1);
                    verify(innerScoreDirector).afterListVariableChanged(variableDescriptor, e1, 0, 1);
                    verify(innerScoreDirector, atLeastOnce()).triggerVariableListeners();
                    return null;
                });

        // v2 -> e1[0]
        moveDirector.execute(new ListAssignMove<>(variableDescriptor, v2, e1, 0));
        // v3 -> e1[1]
        moveDirector.execute(new ListAssignMove<>(variableDescriptor, v3, e1, 1));
        // v1 -> e1[0]
        moveDirector.execute(new ListAssignMove<>(variableDescriptor, v1, e1, 0));
        assertThat(e1.getValueList()).containsExactly(v1, v2, v3);
    }

    @Disabled("Temporarily disabled")
    @Test
    void isMoveDoableValueRangeProviderOnEntity() {
        var v1 = new TestdataListEntityProvidingValue("1");
        var v2 = new TestdataListEntityProvidingValue("2");
        var v3 = new TestdataListEntityProvidingValue("3");
        var e1 = new TestdataListEntityProvidingEntity("e1", List.of(v1, v2));
        var e2 = new TestdataListEntityProvidingEntity("e2", List.of(v1, v3));
        // different entity => valid value
        assertThat(new ListAssignMove<>(otherVariableDescriptor, v1, e1, 0).isMoveDoable(otherInnerScoreDirector))
                .isTrue();
        assertThat(new ListAssignMove<>(otherVariableDescriptor, v1, e2, 0).isMoveDoable(otherInnerScoreDirector))
                .isTrue();
        // different entity => invalid value
        assertThat(new ListAssignMove<>(otherVariableDescriptor, v3, e1, 0).isMoveDoable(otherInnerScoreDirector))
                .isFalse();
    }

    @Test
    void rebase() {
        var v1 = new TestdataListValue("1");
        var e1 = new TestdataListEntity("e1");

        var destinationV1 = new TestdataListValue("1");
        var destinationE1 = new TestdataListEntity("e1");

        ScoreDirector<TestdataListSolution> destinationScoreDirector = mockRebasingScoreDirector(
                variableDescriptor.getEntityDescriptor().getSolutionDescriptor(), new Object[][] {
                        { v1, destinationV1 },
                        { e1, destinationE1 },
                });

        assertSameProperties(
                destinationV1, destinationE1, 0,
                new ListAssignMove<>(variableDescriptor, v1, e1, 0).rebase(destinationScoreDirector));
    }

    static void assertSameProperties(Object movedValue, Object destinationEntity, int destinationIndex,
            ListAssignMove<?> move) {
        assertThat(move.getMovedValue()).isSameAs(movedValue);
        assertThat(move.getDestinationEntity()).isSameAs(destinationEntity);
        assertThat(move.getDestinationIndex()).isEqualTo(destinationIndex);
    }

    @Test
    void toStringTest() {
        var v1 = new TestdataListValue("1");
        var e1 = new TestdataListEntity("E1");

        assertThat(new ListAssignMove<>(variableDescriptor, v1, e1, 15)).hasToString("1 {null -> E1[15]}");
    }
}
