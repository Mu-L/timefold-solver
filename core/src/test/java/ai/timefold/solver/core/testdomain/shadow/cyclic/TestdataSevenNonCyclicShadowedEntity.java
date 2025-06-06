package ai.timefold.solver.core.testdomain.shadow.cyclic;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import ai.timefold.solver.core.testdomain.DummyVariableListener;
import ai.timefold.solver.core.testdomain.TestdataObject;
import ai.timefold.solver.core.testdomain.TestdataValue;

@PlanningEntity
public class TestdataSevenNonCyclicShadowedEntity extends TestdataObject {

    public static EntityDescriptor<TestdataSevenNonCyclicShadowedSolution> buildEntityDescriptor() {
        return TestdataSevenNonCyclicShadowedSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataSevenNonCyclicShadowedEntity.class);
    }

    public static GenuineVariableDescriptor<TestdataSevenNonCyclicShadowedSolution> buildVariableDescriptorForValue() {
        return buildEntityDescriptor().getGenuineVariableDescriptor("value");
    }

    private TestdataValue value;
    // Intentionally out of order
    private String thirdShadow;
    private String fifthShadow;
    private String firstShadow;
    private String fourthShadow;
    private String secondShadow;
    private String seventhShadow;
    private String sixthShadow;

    public TestdataSevenNonCyclicShadowedEntity() {
    }

    public TestdataSevenNonCyclicShadowedEntity(String code) {
        super(code);
    }

    public TestdataSevenNonCyclicShadowedEntity(String code, TestdataValue value) {
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

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "secondShadow")
    public String getThirdShadow() {
        return thirdShadow;
    }

    public void setThirdShadow(String thirdShadow) {
        this.thirdShadow = thirdShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "fourthShadow")
    public String getFifthShadow() {
        return fifthShadow;
    }

    public void setFifthShadow(String fifthShadow) {
        this.fifthShadow = fifthShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "value")
    public String getFirstShadow() {
        return firstShadow;
    }

    public void setFirstShadow(String firstShadow) {
        this.firstShadow = firstShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "thirdShadow")
    public String getFourthShadow() {
        return fourthShadow;
    }

    public void setFourthShadow(String fourthShadow) {
        this.fourthShadow = fourthShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "firstShadow")
    public String getSecondShadow() {
        return secondShadow;
    }

    public void setSecondShadow(String secondShadow) {
        this.secondShadow = secondShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "sixthShadow")
    public String getSeventhShadow() {
        return seventhShadow;
    }

    public void setSeventhShadow(String seventhShadow) {
        this.seventhShadow = seventhShadow;
    }

    @ShadowVariable(variableListenerClass = DummyVariableListener.class, sourceVariableName = "fifthShadow")
    public String getSixthShadow() {
        return sixthShadow;
    }

    public void setSixthShadow(String sixthShadow) {
        this.sixthShadow = sixthShadow;
    }

}
