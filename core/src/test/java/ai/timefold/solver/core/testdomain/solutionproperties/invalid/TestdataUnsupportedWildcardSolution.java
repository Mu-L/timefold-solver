package ai.timefold.solver.core.testdomain.solutionproperties.invalid;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.testdomain.TestdataEntity;
import ai.timefold.solver.core.testdomain.TestdataObject;
import ai.timefold.solver.core.testdomain.TestdataValue;

@PlanningSolution
public class TestdataUnsupportedWildcardSolution extends TestdataObject {

    public static SolutionDescriptor<TestdataUnsupportedWildcardSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataUnsupportedWildcardSolution.class,
                TestdataEntity.class);
    }

    private List<TestdataValue> valueList;
    private List<? super TestdataEntity> supersEntityList;

    private SimpleScore score;

    public TestdataUnsupportedWildcardSolution() {
    }

    public TestdataUnsupportedWildcardSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    public List<TestdataValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<TestdataValue> valueList) {
        this.valueList = valueList;
    }

    @PlanningEntityCollectionProperty
    public List<? super TestdataEntity> getSupersEntityList() {
        return supersEntityList;
    }

    public void setSupersEntityList(List<? super TestdataEntity> supersEntityList) {
        this.supersEntityList = supersEntityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

}
