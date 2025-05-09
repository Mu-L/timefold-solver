package ai.timefold.solver.core.impl.domain.valuerange.buildin.biginteger;

import static ai.timefold.solver.core.testutil.PlannerAssert.assertAllElementsOfIterator;
import static ai.timefold.solver.core.testutil.PlannerAssert.assertElementsOfIterator;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import ai.timefold.solver.core.testutil.TestRandom;

import org.junit.jupiter.api.Test;

class BigIntegerValueRangeTest {

    @Test
    void getSize() {
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10")).getSize()).isEqualTo(10L);
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120")).getSize()).isEqualTo(20L);
        assertThat(new BigIntegerValueRange(new BigInteger("-15"), new BigInteger("25")).getSize()).isEqualTo(40L);
        assertThat(new BigIntegerValueRange(new BigInteger("7"), new BigInteger("7")).getSize()).isEqualTo(0L);
        // IncrementUnit
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2")).getSize())
                .isEqualTo(5L);
        assertThat(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2")).getSize())
                .isEqualTo(5L);
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5")).getSize())
                .isEqualTo(4L);
    }

    @Test
    void get() {
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10")).get(3L)).isEqualTo(new BigInteger("3"));
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120")).get(3L))
                .isEqualTo(new BigInteger("103"));
        assertThat(new BigIntegerValueRange(new BigInteger("-5"), new BigInteger("25")).get(1L))
                .isEqualTo(new BigInteger("-4"));
        assertThat(new BigIntegerValueRange(new BigInteger("-5"), new BigInteger("25")).get(6L)).isEqualTo(new BigInteger("1"));
        // IncrementUnit
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2")).get(3L))
                .isEqualTo(new BigInteger("6"));
        assertThat(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2")).get(3L))
                .isEqualTo(new BigInteger("5"));
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5")).get(3L))
                .isEqualTo(new BigInteger("115"));
    }

    @Test
    void contains() {
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10")).contains(new BigInteger("3"))).isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10")).contains(new BigInteger("10")))
                .isFalse();
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10")).contains(null)).isFalse();
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120")).contains(new BigInteger("100")))
                .isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120")).contains(new BigInteger("99")))
                .isFalse();
        assertThat(new BigIntegerValueRange(new BigInteger("-5"), new BigInteger("25")).contains(new BigInteger("-4")))
                .isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("-5"), new BigInteger("25")).contains(new BigInteger("-20")))
                .isFalse();
        // IncrementUnit
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2"))
                .contains(new BigInteger("2"))).isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2"))
                .contains(new BigInteger("3"))).isFalse();
        assertThat(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2"))
                .contains(new BigInteger("1"))).isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2"))
                .contains(new BigInteger("2"))).isFalse();
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5"))
                .contains(new BigInteger("115"))).isTrue();
        assertThat(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5"))
                .contains(new BigInteger("114"))).isFalse();
    }

    @Test
    void createOriginalIterator() {
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("4"))
                .createOriginalIterator(), new BigInteger("0"), new BigInteger("1"), new BigInteger("2"),
                new BigInteger("3"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("104"))
                .createOriginalIterator(), new BigInteger("100"), new BigInteger("101"), new BigInteger("102"),
                new BigInteger("103"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("-4"), new BigInteger("3"))
                .createOriginalIterator(), new BigInteger("-4"), new BigInteger("-3"), new BigInteger("-2"),
                new BigInteger("-1"), new BigInteger("0"), new BigInteger("1"), new BigInteger("2"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("7"), new BigInteger("7"))
                .createOriginalIterator());
        // IncrementUnit
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2"))
                .createOriginalIterator(), new BigInteger("0"), new BigInteger("2"), new BigInteger("4"),
                new BigInteger("6"), new BigInteger("8"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2"))
                .createOriginalIterator(), new BigInteger("-1"), new BigInteger("1"), new BigInteger("3"),
                new BigInteger("5"), new BigInteger("7"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5"))
                .createOriginalIterator(), new BigInteger("100"), new BigInteger("105"), new BigInteger("110"),
                new BigInteger("115"));
    }

    @Test
    void createRandomIterator() {
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("7"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("3"), new BigInteger("0"));
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("104"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("103"), new BigInteger("100"));
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("-4"), new BigInteger("3"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("-1"), new BigInteger("-4"));
        assertAllElementsOfIterator(new BigIntegerValueRange(new BigInteger("7"), new BigInteger("7"))
                .createRandomIterator(new TestRandom(0)));
        // IncrementUnit
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("0"), new BigInteger("10"), new BigInteger("2"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("6"), new BigInteger("0"));
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("-1"), new BigInteger("9"), new BigInteger("2"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("5"), new BigInteger("-1"));
        assertElementsOfIterator(new BigIntegerValueRange(new BigInteger("100"), new BigInteger("120"), new BigInteger("5"))
                .createRandomIterator(new TestRandom(3, 0)), new BigInteger("115"), new BigInteger("100"));
    }

}
