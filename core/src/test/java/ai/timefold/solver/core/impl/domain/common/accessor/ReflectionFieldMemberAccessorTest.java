package ai.timefold.solver.core.impl.domain.common.accessor;

import static org.assertj.core.api.Assertions.assertThat;

import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testdomain.reflect.field.TestdataFieldAnnotatedEntity;

import org.junit.jupiter.api.Test;

class ReflectionFieldMemberAccessorTest {

    @Test
    void fieldAnnotatedEntity() throws NoSuchFieldException {
        ReflectionFieldMemberAccessor memberAccessor = new ReflectionFieldMemberAccessor(
                TestdataFieldAnnotatedEntity.class.getDeclaredField("value"));
        assertThat(memberAccessor.getName()).isEqualTo("value");
        assertThat(memberAccessor.getType()).isEqualTo(TestdataValue.class);
        assertThat(memberAccessor.getAnnotation(PlanningVariable.class)).isNotNull();

        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataFieldAnnotatedEntity e1 = new TestdataFieldAnnotatedEntity("e1", v1);
        assertThat(memberAccessor.executeGetter(e1)).isSameAs(v1);
        memberAccessor.executeSetter(e1, v2);
        assertThat(e1.getValue()).isSameAs(v2);
    }

}
