package ai.timefold.solver.core.testdomain.valuerange.incomplete;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.testdomain.TestdataObject;
import ai.timefold.solver.core.testdomain.TestdataValue;

@PlanningEntity
public class TestdataIncompleteValueRangeEntity extends TestdataObject {

    public static EntityDescriptor<TestdataIncompleteValueRangeSolution> buildEntityDescriptor() {
        return TestdataIncompleteValueRangeSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataIncompleteValueRangeEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataIncompleteValueRangeSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataValue value;

    public TestdataIncompleteValueRangeEntity() {
    }

    public TestdataIncompleteValueRangeEntity(String code) {
        super(code);
    }

    public TestdataIncompleteValueRangeEntity(String code, TestdataValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getValue() {
        return value;
    }

    public void setValue(TestdataValue value) {
        this.value = value;
    }

}