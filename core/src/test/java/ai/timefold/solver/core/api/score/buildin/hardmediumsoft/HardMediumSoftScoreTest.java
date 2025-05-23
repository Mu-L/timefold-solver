package ai.timefold.solver.core.api.score.buildin.hardmediumsoft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ai.timefold.solver.core.api.score.buildin.AbstractScoreTest;
import ai.timefold.solver.core.testutil.PlannerAssert;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class HardMediumSoftScoreTest extends AbstractScoreTest {

    @Test
    void of() {
        assertThat(HardMediumSoftScore.ofHard(-147)).isEqualTo(HardMediumSoftScore.of(-147, 0, 0));
        assertThat(HardMediumSoftScore.ofMedium(-258)).isEqualTo(HardMediumSoftScore.of(0, -258, 0));
        assertThat(HardMediumSoftScore.ofSoft(-369)).isEqualTo(HardMediumSoftScore.of(0, 0, -369));
    }

    @Test
    void parseScore() {
        assertThat(HardMediumSoftScore.parseScore("-147hard/-258medium/-369soft"))
                .isEqualTo(HardMediumSoftScore.of(-147, -258, -369));
        assertThat(HardMediumSoftScore.parseScore("-147hard/-258medium/*soft"))
                .isEqualTo(HardMediumSoftScore.of(-147, -258, Integer.MIN_VALUE));
        assertThat(HardMediumSoftScore.parseScore("-147hard/*medium/-369soft"))
                .isEqualTo(HardMediumSoftScore.of(-147, Integer.MIN_VALUE, -369));
    }

    @Test
    void toShortString() {
        assertThat(HardMediumSoftScore.of(0, 0, 0).toShortString()).isEqualTo("0");
        assertThat(HardMediumSoftScore.of(0, 0, -369).toShortString()).isEqualTo("-369soft");
        assertThat(HardMediumSoftScore.of(0, -258, 0).toShortString()).isEqualTo("-258medium");
        assertThat(HardMediumSoftScore.of(0, -258, -369).toShortString()).isEqualTo("-258medium/-369soft");
        assertThat(HardMediumSoftScore.of(-147, -258, -369).toShortString()).isEqualTo("-147hard/-258medium/-369soft");
    }

    @Test
    void testToString() {
        assertThat(HardMediumSoftScore.of(0, -258, -369)).hasToString("0hard/-258medium/-369soft");
        assertThat(HardMediumSoftScore.of(-147, -258, -369)).hasToString("-147hard/-258medium/-369soft");
    }

    @Test
    void parseScoreIllegalArgument() {
        assertThatIllegalArgumentException().isThrownBy(() -> HardMediumSoftScore.parseScore("-147"));
    }

    @Test
    void feasible() {
        assertScoreNotFeasible(HardMediumSoftScore.of(-5, -300, -4000));
        assertScoreFeasible(HardMediumSoftScore.of(0, -300, -4000),
                HardMediumSoftScore.of(2, -300, -4000));
    }

    @Test
    void add() {
        assertThat(HardMediumSoftScore.of(20, -20, -4000).add(
                HardMediumSoftScore.of(-1, -300, 4000))).isEqualTo(HardMediumSoftScore.of(19, -320, 0));
    }

    @Test
    void subtract() {
        assertThat(HardMediumSoftScore.of(20, -20, -4000).subtract(
                HardMediumSoftScore.of(-1, -300, 4000))).isEqualTo(HardMediumSoftScore.of(21, 280, -8000));
    }

    @Test
    void multiply() {
        assertThat(HardMediumSoftScore.of(5, -5, 5).multiply(1.2)).isEqualTo(HardMediumSoftScore.of(6, -6, 6));
        assertThat(HardMediumSoftScore.of(1, -1, 1).multiply(1.2)).isEqualTo(HardMediumSoftScore.of(1, -2, 1));
        assertThat(HardMediumSoftScore.of(4, -4, 4).multiply(1.2)).isEqualTo(HardMediumSoftScore.of(4, -5, 4));
    }

    @Test
    void divide() {
        assertThat(HardMediumSoftScore.of(25, -25, 25).divide(5.0)).isEqualTo(HardMediumSoftScore.of(5, -5, 5));
        assertThat(HardMediumSoftScore.of(21, -21, 21).divide(5.0)).isEqualTo(HardMediumSoftScore.of(4, -5, 4));
        assertThat(HardMediumSoftScore.of(24, -24, 24).divide(5.0)).isEqualTo(HardMediumSoftScore.of(4, -5, 4));
    }

    @Test
    void power() {
        assertThat(HardMediumSoftScore.of(3, -4, 5).power(2.0)).isEqualTo(HardMediumSoftScore.of(9, 16, 25));
        assertThat(HardMediumSoftScore.of(9, 16, 25).power(0.5)).isEqualTo(HardMediumSoftScore.of(3, 4, 5));
    }

    @Test
    void negate() {
        assertThat(HardMediumSoftScore.of(3, -4, 5).negate()).isEqualTo(HardMediumSoftScore.of(-3, 4, -5));
        assertThat(HardMediumSoftScore.of(-3, 4, -5).negate()).isEqualTo(HardMediumSoftScore.of(3, -4, 5));
    }

    @Test
    void abs() {
        assertThat(HardMediumSoftScore.of(3, 4, 5).abs()).isEqualTo(HardMediumSoftScore.of(3, 4, 5));
        assertThat(HardMediumSoftScore.of(3, -4, 5).abs()).isEqualTo(HardMediumSoftScore.of(3, 4, 5));
        assertThat(HardMediumSoftScore.of(-3, 4, -5).abs()).isEqualTo(HardMediumSoftScore.of(3, 4, 5));
        assertThat(HardMediumSoftScore.of(-3, -4, -5).abs()).isEqualTo(HardMediumSoftScore.of(3, 4, 5));
    }

    @Test
    void zero() {
        HardMediumSoftScore manualZero = HardMediumSoftScore.of(0, 0, 0);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(manualZero.zero()).isEqualTo(manualZero);
            softly.assertThat(manualZero.isZero()).isTrue();
            HardMediumSoftScore manualOne = HardMediumSoftScore.of(0, 0, 1);
            softly.assertThat(manualOne.isZero()).isFalse();
        });
    }

    @Test
    void equalsAndHashCode() {
        PlannerAssert.assertObjectsAreEqual(
                HardMediumSoftScore.of(-10, -200, -3000),
                HardMediumSoftScore.of(-10, -200, -3000));
        PlannerAssert.assertObjectsAreNotEqual(
                HardMediumSoftScore.of(-10, -200, -3000),
                HardMediumSoftScore.of(-30, -200, -3000),
                HardMediumSoftScore.of(-10, -400, -3000),
                HardMediumSoftScore.of(-10, -400, -5000));
    }

    @Test
    void compareTo() {
        PlannerAssert.assertCompareToOrder(
                HardMediumSoftScore.of(-20, Integer.MIN_VALUE, Integer.MIN_VALUE),
                HardMediumSoftScore.of(-20, Integer.MIN_VALUE, -20),
                HardMediumSoftScore.of(-20, Integer.MIN_VALUE, 1),
                HardMediumSoftScore.of(-20, -300, -4000),
                HardMediumSoftScore.of(-20, -300, -300),
                HardMediumSoftScore.of(-20, -300, -20),
                HardMediumSoftScore.of(-20, -300, 300),
                HardMediumSoftScore.of(-20, -20, -300),
                HardMediumSoftScore.of(-20, -20, 0),
                HardMediumSoftScore.of(-20, -20, 1),
                HardMediumSoftScore.of(-1, -300, -4000),
                HardMediumSoftScore.of(-1, -300, -20),
                HardMediumSoftScore.of(-1, -20, -300),
                HardMediumSoftScore.of(1, Integer.MIN_VALUE, -20),
                HardMediumSoftScore.of(1, -20, Integer.MIN_VALUE));
    }
}
