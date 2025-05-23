package ai.timefold.solver.core.testdomain.chained.shadow;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.testdomain.TestdataObject;

@PlanningSolution
public class TestdataShadowingChainedSolution extends TestdataObject {

    public static SolutionDescriptor<TestdataShadowingChainedSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(TestdataShadowingChainedSolution.class,
                TestdataShadowingChainedObject.class, TestdataShadowingChainedEntity.class);
    }

    private List<TestdataShadowingChainedAnchor> chainedAnchorList;
    private List<TestdataShadowingChainedEntity> chainedEntityList;

    private SimpleScore score;

    public TestdataShadowingChainedSolution() {
    }

    public TestdataShadowingChainedSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "chainedAnchorRange")
    @ProblemFactCollectionProperty
    public List<TestdataShadowingChainedAnchor> getChainedAnchorList() {
        return chainedAnchorList;
    }

    public void setChainedAnchorList(List<TestdataShadowingChainedAnchor> chainedAnchorList) {
        this.chainedAnchorList = chainedAnchorList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "chainedEntityRange")
    public List<TestdataShadowingChainedEntity> getChainedEntityList() {
        return chainedEntityList;
    }

    public void setChainedEntityList(List<TestdataShadowingChainedEntity> chainedEntityList) {
        this.chainedEntityList = chainedEntityList;
    }

    @PlanningScore
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
