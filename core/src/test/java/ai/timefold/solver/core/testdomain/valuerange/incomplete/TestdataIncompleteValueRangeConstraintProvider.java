package ai.timefold.solver.core.testdomain.valuerange.incomplete;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.jspecify.annotations.NonNull;

public final class TestdataIncompleteValueRangeConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory constraintFactory) {
        return new Constraint[] { alwaysPenalizingConstraint(constraintFactory) };
    }

    private Constraint alwaysPenalizingConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(TestdataIncompleteValueRangeEntity.class)
                .penalize(SimpleScore.ONE)
                .asConstraint("Always penalize");
    }

}