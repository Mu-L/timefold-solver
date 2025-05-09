package ai.timefold.solver.test.api.testdomain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.testdomain.TestdataValue;

@PlanningSolution
public final class TestdataConstraintVerifierExtendedSolution extends TestdataConstraintVerifierSolution {

    public static TestdataConstraintVerifierExtendedSolution generateSolution(int valueListSize, int entityListSize) {
        TestdataConstraintVerifierExtendedSolution solution =
                new TestdataConstraintVerifierExtendedSolution("Generated Solution 0");
        List<TestdataValue> valueList = new ArrayList<>();
        List<String> secondValueList = new ArrayList<>();
        for (int i = 0; i < valueListSize; i++) {
            TestdataValue value = new TestdataValue("Generated Value " + i);
            valueList.add(value);
            secondValueList.add(value.getCode());
        }
        solution.setValueList(valueList);
        solution.setStringValueList(secondValueList);
        List<TestdataConstraintVerifierFirstEntity> entityList = new ArrayList<>();
        List<TestdataConstraintVerifierSecondEntity> secondEntityList = new ArrayList<>();
        for (int i = 0; i < entityListSize; i++) {
            if (i % 2 == 0) {
                TestdataValue value = valueList.get(i % valueListSize);
                TestdataConstraintVerifierFirstEntity entity =
                        new TestdataConstraintVerifierFirstEntity("Generated Entity " + i, value);
                entityList.add(entity);
            } else {
                String value = secondValueList.get(i / valueListSize);
                TestdataConstraintVerifierSecondEntity entity =
                        new TestdataConstraintVerifierSecondEntity("Generated Entity " + i, value);
                secondEntityList.add(entity);
            }

        }
        solution.setEntityList(entityList);
        solution.setSecondEntityList(secondEntityList);
        return solution;
    }

    private List<String> stringValueList;
    private List<TestdataConstraintVerifierSecondEntity> secondEntityList;

    public TestdataConstraintVerifierExtendedSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "stringValueRange")
    @ProblemFactCollectionProperty
    public List<String> getStringValueList() {
        return stringValueList;
    }

    public void setStringValueList(List<String> stringValueList) {
        this.stringValueList = stringValueList;
    }

    @PlanningEntityCollectionProperty
    public List<TestdataConstraintVerifierSecondEntity> getSecondEntityList() {
        return secondEntityList;
    }

    public void setSecondEntityList(List<TestdataConstraintVerifierSecondEntity> secondEntityList) {
        this.secondEntityList = secondEntityList;
    }
}
