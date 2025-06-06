package ai.timefold.solver.quarkus.testdomain.suppliervariable.simple;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;

@PlanningSolution
public class TestdataQuarkusSupplierVariableSimpleSolution {

    private List<String> valueList;
    private List<TestdataQuarkusSupplierVariableSimpleEntity> entityList;

    private SimpleScore score;

    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataQuarkusSupplierVariableSimpleEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataQuarkusSupplierVariableSimpleEntity> entityList) {
        this.entityList = entityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

}
