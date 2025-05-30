package ai.timefold.solver.core.testdomain.inheritance.entity.single.baseannotated.classes.pinned;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.testdomain.TestdataValue;
import ai.timefold.solver.core.testdomain.pinned.TestdataPinnedEntity;

@PlanningEntity(pinningFilter = TestdataExtendedPinningFilter.class)
public class TestdataExtendedPinnedEntity extends TestdataPinnedEntity {

    private TestdataValue subValue;
    private boolean closed;
    private boolean pinnedByBoss;

    public TestdataExtendedPinnedEntity() {
    }

    public TestdataExtendedPinnedEntity(String code) {
        super(code);
    }

    public TestdataExtendedPinnedEntity(String code, TestdataValue value, TestdataValue subValue) {
        super(code, value);
        this.subValue = subValue;
    }

    public TestdataExtendedPinnedEntity(String code, TestdataValue value, boolean locked, boolean pinned,
            TestdataValue subValue, boolean closed, boolean pinnedByBoss) {
        super(code, value, locked, pinned);
        this.subValue = subValue;
        this.closed = closed;
        this.pinnedByBoss = pinnedByBoss;
    }

    @PlanningVariable(valueRangeProviderRefs = "subValueRange")
    public TestdataValue getSubValue() {
        return subValue;
    }

    public void setSubValue(TestdataValue subValue) {
        this.subValue = subValue;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @PlanningPin
    public boolean isPinnedByBoss() {
        return pinnedByBoss;
    }

    public void setPinnedByBoss(boolean pinnedByBoss) {
        this.pinnedByBoss = pinnedByBoss;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
