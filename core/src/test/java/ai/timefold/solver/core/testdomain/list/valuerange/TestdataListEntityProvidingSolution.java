package ai.timefold.solver.core.testdomain.list.valuerange;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;

@PlanningSolution
public class TestdataListEntityProvidingSolution {

    public static SolutionDescriptor<TestdataListEntityProvidingSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataListEntityProvidingSolution.class,
                TestdataListEntityProvidingEntity.class);
    }

    public static TestdataListEntityProvidingSolution generateSolution() {
        var solution = new TestdataListEntityProvidingSolution();
        var value1 = new TestdataListEntityProvidingValue("v1");
        var value2 = new TestdataListEntityProvidingValue("v2");
        var value3 = new TestdataListEntityProvidingValue("v3");
        var entity1 = new TestdataListEntityProvidingEntity("e1", List.of(value1, value2));
        var entity2 = new TestdataListEntityProvidingEntity("e2", List.of(value1, value3));
        solution.setEntityList(List.of(entity1, entity2));
        return solution;
    }

    private List<TestdataListEntityProvidingEntity> entityList;
    private List<TestdataListEntityProvidingValue> temporaryValueList;

    private SimpleScore score;

    @PlanningEntityCollectionProperty
    public List<TestdataListEntityProvidingEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataListEntityProvidingEntity> entityList) {
        this.entityList = entityList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    public List<TestdataListEntityProvidingValue> getTemporaryValueList() {
        return temporaryValueList;
    }

    public void setTemporaryValueList(List<TestdataListEntityProvidingValue> temporaryValueList) {
        this.temporaryValueList = temporaryValueList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

}
