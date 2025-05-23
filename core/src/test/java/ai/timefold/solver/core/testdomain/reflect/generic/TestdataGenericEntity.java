package ai.timefold.solver.core.testdomain.reflect.generic;

import java.util.Map;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.testdomain.TestdataObject;

@PlanningEntity
public class TestdataGenericEntity<T> extends TestdataObject {

    public static EntityDescriptor<TestdataGenericSolution> buildEntityDescriptor() {
        return TestdataGenericSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataGenericEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataGenericSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataGenericValue<T> value;
    private TestdataGenericValue<T> subTypeValue;
    private TestdataGenericValue<Map<T, TestdataGenericValue<T>>> complexGenericValue;

    public TestdataGenericEntity() {
    }

    public TestdataGenericEntity(String code) {
        super(code);
    }

    public TestdataGenericEntity(String code, TestdataGenericValue value) {
        this(code);
        this.value = value;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataGenericValue<T> getValue() {
        return value;
    }

    @PlanningVariable(valueRangeProviderRefs = "subTypeValueRange")
    public TestdataGenericValue<T> getSubTypeValue() {
        return subTypeValue;
    }

    public void setValue(TestdataGenericValue<T> value) {
        this.value = value;
    }

    public void setSubTypeValue(TestdataGenericValue<T> subTypeValue) {
        this.subTypeValue = subTypeValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "complexGenericValueRange")
    public TestdataGenericValue<Map<T, TestdataGenericValue<T>>> getComplexGenericValue() {
        return complexGenericValue;
    }

    public void setComplexGenericValue(TestdataGenericValue<Map<T, TestdataGenericValue<T>>> complexGenericValue) {
        this.complexGenericValue = complexGenericValue;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
