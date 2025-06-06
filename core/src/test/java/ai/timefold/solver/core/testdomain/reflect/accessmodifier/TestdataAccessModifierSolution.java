package ai.timefold.solver.core.testdomain.reflect.accessmodifier;

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
public class TestdataAccessModifierSolution extends TestdataObject {

    private static final String STATIC_FINAL_FIELD = "staticFinalFieldValue";

    private static Object staticField;

    public static String getStaticFinalField() {
        return STATIC_FINAL_FIELD;
    }

    public static Object getStaticField() {
        return staticField;
    }

    public static void setStaticField(Object staticField) {
        TestdataAccessModifierSolution.staticField = staticField;
    }

    public static SolutionDescriptor<TestdataAccessModifierSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataAccessModifierSolution.class, TestdataEntity.class);
    }

    private final String finalField;
    private String readWriteOnlyField;

    private String privateField;
    public String publicField;
    private String privatePropertyField;
    private String friendlyPropertyField;
    private String protectedPropertyField;
    private String publicPropertyField;

    private List<TestdataValue> valueList;
    private List<TestdataEntity> entityList;

    @PlanningScore
    private SimpleScore score;

    private TestdataAccessModifierSolution() {
        finalField = "No-argument constructor";
    }

    public TestdataAccessModifierSolution(String code) {
        super(code);
        finalField = "Constructor with argument code (" + code + ")";
    }

    public String getFinalField() {
        return finalField;
    }

    public String getReadOnlyField() {
        return "read" + readWriteOnlyField;
    }

    public void setWriteOnlyField(String writeOnlyField) {
        if (!writeOnlyField.startsWith("write")) {
            throw new IllegalArgumentException("The writeOnlyField (" + writeOnlyField + ") should start with write.");
        }
        readWriteOnlyField = writeOnlyField.substring("write".length());
    }

    private String getPrivateProperty() {
        return privatePropertyField;
    }

    private void setPrivateProperty(String privateProperty) {
        this.privatePropertyField = privateProperty;
    }

    String getFriendlyProperty() {
        return friendlyPropertyField;
    }

    void setFriendlyProperty(String friendlyProperty) {
        this.friendlyPropertyField = friendlyProperty;
    }

    protected String getProtectedProperty() {
        return protectedPropertyField;
    }

    protected void setProtectedProperty(String protectedProperty) {
        this.protectedPropertyField = protectedProperty;
    }

    public String getPublicProperty() {
        return publicPropertyField;
    }

    public void setPublicProperty(String publicProperty) {
        this.publicPropertyField = publicProperty;
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
    public List<TestdataEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<TestdataEntity> entityList) {
        this.entityList = entityList;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
