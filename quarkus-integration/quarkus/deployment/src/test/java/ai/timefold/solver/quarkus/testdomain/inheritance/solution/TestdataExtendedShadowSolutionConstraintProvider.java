package ai.timefold.solver.quarkus.testdomain.inheritance.solution;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.testdomain.inheritance.entity.single.baseannotated.classes.shadow.TestdataExtendedShadowEntity;

import org.jspecify.annotations.NonNull;

public class TestdataExtendedShadowSolutionConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory constraintFactory) {
        return new Constraint[] {
                constraintFactory.forEach(TestdataExtendedShadowEntity.class)
                        .filter(e -> e.myPlanningVariable.id != e.desiredId)
                        .penalize(SimpleScore.ONE)
                        .asConstraint("Variable does not match desired id")
        };
    }
}
