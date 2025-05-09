package ai.timefold.solver.core.testdomain.shadow.manytomany;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PiggybackShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.entity.descriptor.EntityDescriptor;
import ai.timefold.solver.core.testdomain.DummyVariableListener;
import ai.timefold.solver.core.testdomain.TestdataObject;
import ai.timefold.solver.core.testdomain.TestdataValue;

import org.jspecify.annotations.NonNull;

@PlanningEntity
public class TestdataManyToManyShadowedEntity extends TestdataObject {

    public static EntityDescriptor<TestdataManyToManyShadowedSolution> buildEntityDescriptor() {
        return TestdataManyToManyShadowedSolution.buildSolutionDescriptor()
                .findEntityDescriptorOrFail(TestdataManyToManyShadowedEntity.class);
    }

    private TestdataValue primaryValue;
    private TestdataValue secondaryValue;
    private String composedCode;
    private String reverseComposedCode;

    public TestdataManyToManyShadowedEntity() {
    }

    public TestdataManyToManyShadowedEntity(String code) {
        super(code);
    }

    public TestdataManyToManyShadowedEntity(String code, TestdataValue primaryValue, TestdataValue secondaryValue) {
        this(code);
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(TestdataValue primaryValue) {
        this.primaryValue = primaryValue;
    }

    @PlanningVariable(valueRangeProviderRefs = "valueRange")
    public TestdataValue getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(TestdataValue secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    @ShadowVariable(variableListenerClass = ComposedValuesUpdatingVariableListener.class, sourceVariableName = "primaryValue")
    @ShadowVariable(variableListenerClass = ComposedValuesUpdatingVariableListener.class, sourceVariableName = "secondaryValue")
    public String getComposedCode() {
        return composedCode;
    }

    public void setComposedCode(String composedCode) {
        this.composedCode = composedCode;
    }

    @PiggybackShadowVariable(shadowVariableName = "composedCode")
    public String getReverseComposedCode() {
        return reverseComposedCode;
    }

    public void setReverseComposedCode(String reverseComposedCode) {
        this.reverseComposedCode = reverseComposedCode;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    // ************************************************************************
    // Static inner classes
    // ************************************************************************

    public static class ComposedValuesUpdatingVariableListener
            extends DummyVariableListener<TestdataManyToManyShadowedSolution, TestdataManyToManyShadowedEntity> {

        @Override
        public void afterEntityAdded(@NonNull ScoreDirector<TestdataManyToManyShadowedSolution> scoreDirector,
                @NonNull TestdataManyToManyShadowedEntity entity) {
            updateShadow(entity, scoreDirector);
        }

        @Override
        public void afterVariableChanged(@NonNull ScoreDirector<TestdataManyToManyShadowedSolution> scoreDirector,
                @NonNull TestdataManyToManyShadowedEntity entity) {
            updateShadow(entity, scoreDirector);
        }

        private void updateShadow(TestdataManyToManyShadowedEntity entity,
                ScoreDirector<TestdataManyToManyShadowedSolution> scoreDirector) {
            TestdataValue primaryValue = entity.getPrimaryValue();
            TestdataValue secondaryValue = entity.getSecondaryValue();
            String composedValue;
            String reverseComposedValue;
            if (primaryValue == null || secondaryValue == null) {
                composedValue = null;
                reverseComposedValue = null;
            } else {
                composedValue = primaryValue.getCode() + "-" + secondaryValue.getCode();
                reverseComposedValue = secondaryValue.getCode() + "-" + primaryValue.getCode();
            }
            scoreDirector.beforeVariableChanged(entity, "composedCode");
            entity.setComposedCode(composedValue);
            scoreDirector.afterVariableChanged(entity, "composedCode");
            scoreDirector.beforeVariableChanged(entity, "reverseComposedCode");
            entity.setReverseComposedCode(reverseComposedValue);
            scoreDirector.afterVariableChanged(entity, "reverseComposedCode");
        }

    }

}
