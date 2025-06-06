package ai.timefold.solver.core.testdomain.chained.shadow;

import ai.timefold.solver.core.testdomain.TestdataObject;

public class TestdataShadowingChainedAnchor extends TestdataObject implements TestdataShadowingChainedObject {

    // Shadow variables
    private TestdataShadowingChainedEntity nextEntity;

    public TestdataShadowingChainedAnchor() {
    }

    public TestdataShadowingChainedAnchor(String code) {
        super(code);
    }

    @Override
    public TestdataShadowingChainedEntity getNextEntity() {
        return nextEntity;
    }

    @Override
    public void setNextEntity(TestdataShadowingChainedEntity nextEntity) {
        this.nextEntity = nextEntity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
