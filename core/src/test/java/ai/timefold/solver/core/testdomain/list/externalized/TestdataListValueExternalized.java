package ai.timefold.solver.core.testdomain.list.externalized;

import ai.timefold.solver.core.testdomain.TestdataObject;

public class TestdataListValueExternalized extends TestdataObject {

    public TestdataListValueExternalized() {
    }

    public TestdataListValueExternalized(String code) {
        super(code);
    }

    @Override
    public boolean equals(Object obj) {
        // Pretend a bad equals() design that makes all values equal. This proves that external supplies must use
        // the IdentityHasMap to eliminate dependency on user domain implementation of equals().
        return obj != null && obj.getClass().equals(this.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
