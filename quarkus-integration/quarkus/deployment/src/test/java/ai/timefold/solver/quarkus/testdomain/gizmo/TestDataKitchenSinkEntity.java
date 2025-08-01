package ai.timefold.solver.quarkus.testdomain.gizmo;

import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.CustomShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PiggybackShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableReference;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.preview.api.domain.variable.declarative.ShadowSources;
import ai.timefold.solver.core.preview.api.domain.variable.declarative.ShadowVariableLooped;

/*
 *  Should have one of every annotation, even annotations that
 *  don't make sense on an entity, to make sure everything works
 *  a-ok.
 */
@PlanningEntity
public class TestDataKitchenSinkEntity {

    private String groupId;
    private Integer intVariable;

    @CustomShadowVariable(
            variableListenerClass = DummyVariableListener.class,
            sources = {
                    @PlanningVariableReference(entityClass = TestDataKitchenSinkEntity.class,
                            variableName = "stringVariable")
            })
    private String shadow1;

    @ShadowVariable(
            variableListenerClass = DummyVariableListener.class,
            sourceEntityClass = TestDataKitchenSinkEntity.class, sourceVariableName = "stringVariable")
    private String shadow2;

    @ShadowVariable(supplierName = "copyStringVariable")
    private String declarativeShadowVariable;

    @ShadowVariableLooped
    private boolean shadowVariableLooped;

    @PiggybackShadowVariable(shadowVariableName = "shadow2")
    private String piggybackShadow;

    @PlanningVariable(valueRangeProviderRefs = { "names" })
    private String stringVariable;

    private boolean isPinned;

    @PlanningVariable(valueRangeProviderRefs = { "ints" })
    public Integer getIntVariable() {
        return intVariable;
    }

    public void setIntVariable(Integer val) {
        intVariable = val;
    }

    public Integer testGetIntVariable() {
        return intVariable;
    }

    public String testGetStringVariable() {
        return stringVariable;
    }

    @ShadowSources(value = "stringVariable", alignmentKey = "groupId")
    private String copyStringVariable() {
        return stringVariable;
    }

    @PlanningPin
    private boolean isPinned() {
        return isPinned;
    }

    @ValueRangeProvider(id = "ints")
    private List<Integer> myIntValueRange() {
        return Collections.singletonList(1);
    }

    @ValueRangeProvider(id = "names")
    public List<String> myStringValueRange() {
        return Collections.singletonList("A");
    }

}
