package ai.timefold.solver.core.api.score.stream;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import ai.timefold.solver.core.api.function.PentaPredicate;
import ai.timefold.solver.core.api.function.QuadFunction;
import ai.timefold.solver.core.api.function.QuadPredicate;
import ai.timefold.solver.core.api.function.TriFunction;
import ai.timefold.solver.core.api.function.TriPredicate;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream;
import ai.timefold.solver.core.api.score.stream.bi.BiJoiner;
import ai.timefold.solver.core.api.score.stream.penta.PentaJoiner;
import ai.timefold.solver.core.api.score.stream.quad.QuadJoiner;
import ai.timefold.solver.core.api.score.stream.tri.TriJoiner;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import ai.timefold.solver.core.impl.bavet.bi.joiner.DefaultBiJoiner;
import ai.timefold.solver.core.impl.bavet.bi.joiner.FilteringBiJoiner;
import ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType;
import ai.timefold.solver.core.impl.bavet.penta.joiner.DefaultPentaJoiner;
import ai.timefold.solver.core.impl.bavet.penta.joiner.FilteringPentaJoiner;
import ai.timefold.solver.core.impl.bavet.quad.joiner.DefaultQuadJoiner;
import ai.timefold.solver.core.impl.bavet.quad.joiner.FilteringQuadJoiner;
import ai.timefold.solver.core.impl.bavet.tri.joiner.DefaultTriJoiner;
import ai.timefold.solver.core.impl.bavet.tri.joiner.FilteringTriJoiner;
import ai.timefold.solver.core.impl.util.ConstantLambdaUtils;

import org.jspecify.annotations.NonNull;

/**
 * Creates an {@link BiJoiner}, {@link TriJoiner}, ... instance
 * for use in {@link UniConstraintStream#join(Class, BiJoiner)}, ...
 */
public final class Joiners {

    // TODO Support using non-natural comparators, such as lessThan(leftMapping, rightMapping, comparator).
    // TODO Support collection-based joiners, such as containing(), intersecting() and disjoint().

    // ************************************************************************
    // BiJoiner
    // ************************************************************************

    /**
     * As defined by {@link #equal(Function)} with {@link Function#identity()} as the argument.
     *
     * @param <A> the type of both objects
     */
    public static <A> @NonNull BiJoiner<A, A> equal() {
        return equal(ConstantLambdaUtils.identity());
    }

    /**
     * As defined by {@link #equal(Function, Function)} with both arguments using the same mapping.
     *
     * @param <A> the type of both objects
     * @param <Property_> the type of the property to compare
     * @param mapping mapping function to apply to both A and B
     */
    public static <A, Property_> @NonNull BiJoiner<A, A> equal(Function<A, Property_> mapping) {
        return equal(mapping, mapping);
    }

    /**
     * Joins every A and B that share a property.
     * These are exactly the pairs where {@code leftMapping.apply(A).equals(rightMapping.apply(B))}.
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with both leftMapping and rightMapping being {@code Person::getAge},
     * this joiner will produce pairs {@code (Ann, Ann), (Ann, Eric), (Beth, Beth), (Eric, Ann), (Eric, Eric)}.
     *
     * @param <B> the type of object on the right
     * @param <Property_> the type of the property to compare
     * @param leftMapping mapping function to apply to A
     * @param rightMapping mapping function to apply to B
     */
    public static <A, B, Property_> @NonNull BiJoiner<A, B> equal(Function<A, Property_> leftMapping,
            Function<B, Property_> rightMapping) {
        return new DefaultBiJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #lessThan(Function, Function)} with both arguments using the same mapping.
     *
     * @param mapping mapping function to apply
     * @param <A> the type of both objects
     * @param <Property_> the type of the property to compare
     */
    public static <A, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, A>
            lessThan(Function<A, Property_> mapping) {
        return lessThan(mapping, mapping);
    }

    /**
     * Joins every A and B where a value of property on A is less than the value of a property on B.
     * These are exactly the pairs where {@code leftMapping.apply(A).compareTo(rightMapping.apply(B)) < 0}.
     *
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with both leftMapping and rightMapping being {@code Person::getAge},
     * this joiner will produce pairs {@code (Ann, Beth), (Eric, Beth)}.
     *
     * @param leftMapping mapping function to apply to A
     * @param rightMapping mapping function to apply to B
     * @param <A> the type of object on the left
     * @param <B> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, B> lessThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new DefaultBiJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    /**
     * As defined by {@link #lessThanOrEqual(Function, Function)} with both arguments using the same mapping.
     *
     * @param mapping mapping function to apply
     * @param <A> the type of both objects
     * @param <Property_> the type of the property to compare
     */
    public static <A, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, A> lessThanOrEqual(
            Function<A, Property_> mapping) {
        return lessThanOrEqual(mapping, mapping);
    }

    /**
     * Joins every A and B where a value of property on A is less than or equal to the value of a property on B.
     * These are exactly the pairs where {@code leftMapping.apply(A).compareTo(rightMapping.apply(B)) <= 0}.
     *
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with both leftMapping and rightMapping being {@code Person::getAge},
     * this joiner will produce pairs
     * {@code (Ann, Ann), (Ann, Beth), (Ann, Eric), (Beth, Beth), (Eric, Ann), (Eric, Beth), (Eric, Eric)}.
     *
     * @param leftMapping mapping function to apply to A
     * @param rightMapping mapping function to apply to B
     * @param <A> the type of object on the left
     * @param <B> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, B> lessThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new DefaultBiJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #greaterThan(Function, Function)} with both arguments using the same mapping.
     *
     * @param mapping mapping function to apply
     * @param <A> the type of both objects
     * @param <Property_> the type of the property to compare
     */
    public static <A, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, A> greaterThan(
            Function<A, Property_> mapping) {
        return greaterThan(mapping, mapping);
    }

    /**
     * Joins every A and B where a value of property on A is greater than the value of a property on B.
     * These are exactly the pairs where {@code leftMapping.apply(A).compareTo(rightMapping.apply(B)) > 0}.
     *
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with both leftMapping and rightMapping being {@code Person::getAge},
     * this joiner will produce pairs {@code (Beth, Ann), (Beth, Eric)}.
     *
     * @param leftMapping mapping function to apply to A
     * @param rightMapping mapping function to apply to B
     * @param <A> the type of object on the left
     * @param <B> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, B> greaterThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new DefaultBiJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    /**
     * As defined by {@link #greaterThanOrEqual(Function, Function)} with both arguments using the same mapping.
     *
     * @param mapping mapping function to apply
     * @param <A> the type of both objects
     * @param <Property_> the type of the property to compare
     */
    public static <A, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, A> greaterThanOrEqual(
            Function<A, Property_> mapping) {
        return greaterThanOrEqual(mapping, mapping);
    }

    /**
     * Joins every A and B where a value of property on A is greater than or equal to the value of a property on B.
     * These are exactly the pairs where {@code leftMapping.apply(A).compareTo(rightMapping.apply(B)) >= 0}.
     *
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with both leftMapping and rightMapping being {@code Person::getAge},
     * this joiner will produce pairs
     * {@code (Ann, Ann), (Ann, Eric), (Beth, Ann), (Beth, Beth), (Beth, Eric), (Eric, Ann), (Eric, Eric)}.
     *
     * @param leftMapping mapping function to apply to A
     * @param rightMapping mapping function to apply to B
     * @param <A> the type of object on the left
     * @param <B> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, B> greaterThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new DefaultBiJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * Applies a filter to the joined tuple, with the semantics of {@link BiConstraintStream#filter(BiPredicate)}.
     *
     * For example, on a cartesian product of list {@code [Ann(age = 20), Beth(age = 25), Eric(age = 20)]}
     * with filter being {@code age == 20},
     * this joiner will produce pairs {@code (Ann, Ann), (Ann, Eric), (Eric, Ann), (Eric, Eric)}.
     *
     * @param filter filter to apply
     * @param <A> type of the first fact in the tuple
     * @param <B> type of the second fact in the tuple
     */
    public static <A, B> @NonNull BiJoiner<A, B> filtering(@NonNull BiPredicate<A, B> filter) {
        return new FilteringBiJoiner<>(filter);
    }

    /**
     * Joins every A and B that overlap for an interval which is specified by a start and end property on both A and B.
     * These are exactly the pairs where {@code A.start < B.end} and {@code A.end > B.start}.
     *
     * For example, on a cartesian product of list
     * {@code [Ann(start=08:00, end=14:00), Beth(start=12:00, end=18:00), Eric(start=16:00, end=22:00)]}
     * with startMapping being {@code Person::getStart} and endMapping being {@code Person::getEnd},
     * this joiner will produce pairs
     * {@code (Ann, Ann), (Ann, Beth), (Beth, Ann), (Beth, Beth), (Beth, Eric), (Eric, Beth), (Eric, Eric)}.
     *
     * @param startMapping maps the argument to the start point of its interval (inclusive)
     * @param endMapping maps the argument to the end point of its interval (exclusive)
     * @param <A> the type of both the first and second argument
     * @param <Property_> the type used to define the interval, comparable
     */
    public static <A, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, A> overlapping(
            Function<A, Property_> startMapping, Function<A, Property_> endMapping) {
        return overlapping(startMapping, endMapping, startMapping, endMapping);
    }

    /**
     * As defined by {@link #overlapping(Function, Function)}.
     *
     * @param leftStartMapping maps the first argument to its interval start point (inclusive)
     * @param leftEndMapping maps the first argument to its interval end point (exclusive)
     * @param rightStartMapping maps the second argument to its interval start point (inclusive)
     * @param rightEndMapping maps the second argument to its interval end point (exclusive)
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <Property_> the type used to define the interval, comparable
     */
    public static <A, B, Property_ extends Comparable<Property_>> @NonNull BiJoiner<A, B> overlapping(
            Function<A, Property_> leftStartMapping, Function<A, Property_> leftEndMapping,
            Function<B, Property_> rightStartMapping, Function<B, Property_> rightEndMapping) {
        return Joiners.lessThan(leftStartMapping, rightEndMapping)
                .and(Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    // ************************************************************************
    // TriJoiner
    // ************************************************************************

    /**
     * As defined by {@link #equal(Function, Function)}.
     *
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the object on the right
     * @param <Property_> the type of the property to compare
     * @param leftMapping mapping function to apply to (A,B)
     * @param rightMapping mapping function to apply to C
     */
    public static <A, B, C, Property_> @NonNull TriJoiner<A, B, C> equal(BiFunction<A, B, Property_> leftMapping,
            Function<C, Property_> rightMapping) {
        return new DefaultTriJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #lessThan(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B)
     * @param rightMapping mapping function to apply to C
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> @NonNull TriJoiner<A, B, C> lessThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new DefaultTriJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    /**
     * As defined by {@link #lessThanOrEqual(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B)
     * @param rightMapping mapping function to apply to C
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> @NonNull TriJoiner<A, B, C> lessThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new DefaultTriJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #greaterThan(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B)
     * @param rightMapping mapping function to apply to C
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> @NonNull TriJoiner<A, B, C> greaterThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new DefaultTriJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    /**
     * As defined by {@link #greaterThanOrEqual(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B)
     * @param rightMapping mapping function to apply to C
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> @NonNull TriJoiner<A, B, C> greaterThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new DefaultTriJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #filtering(BiPredicate)}.
     *
     * @param filter filter to apply
     * @param <A> type of the first fact in the tuple
     * @param <B> type of the second fact in the tuple
     * @param <C> type of the third fact in the tuple
     */
    public static <A, B, C> @NonNull TriJoiner<A, B, C> filtering(@NonNull TriPredicate<A, B, C> filter) {
        return new FilteringTriJoiner<>(filter);
    }

    /**
     * As defined by {@link #overlapping(Function, Function)}.
     *
     * @param leftStartMapping maps the first and second arguments to their interval start point (inclusive)
     * @param leftEndMapping maps the first and second arguments to their interval end point (exclusive)
     * @param rightStartMapping maps the third argument to its interval start point (inclusive)
     * @param rightEndMapping maps the third argument to its interval end point (exclusive)
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <Property_> the type used to define the interval, comparable
     */
    public static <A, B, C, Property_ extends Comparable<Property_>> @NonNull TriJoiner<A, B, C> overlapping(
            BiFunction<A, B, Property_> leftStartMapping, BiFunction<A, B, Property_> leftEndMapping,
            Function<C, Property_> rightStartMapping, Function<C, Property_> rightEndMapping) {
        return Joiners.lessThan(leftStartMapping, rightEndMapping)
                .and(Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    // ************************************************************************
    // QuadJoiner
    // ************************************************************************

    /**
     * As defined by {@link #equal(Function, Function)}.
     *
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the object on the right
     * @param <Property_> the type of the property to compare
     * @param leftMapping mapping function to apply to (A, B, C)
     * @param rightMapping mapping function to apply to D
     */
    public static <A, B, C, D, Property_> @NonNull QuadJoiner<A, B, C, D> equal(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new DefaultQuadJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #lessThan(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B,C)
     * @param rightMapping mapping function to apply to D
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> @NonNull QuadJoiner<A, B, C, D> lessThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new DefaultQuadJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    /**
     * As defined by {@link #lessThanOrEqual(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B,C)
     * @param rightMapping mapping function to apply to D
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> @NonNull QuadJoiner<A, B, C, D> lessThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new DefaultQuadJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #greaterThan(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B,C)
     * @param rightMapping mapping function to apply to D
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> @NonNull QuadJoiner<A, B, C, D> greaterThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new DefaultQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    /**
     * As defined by {@link #greaterThanOrEqual(Function, Function)}.
     *
     * @param leftMapping mapping function to apply to (A,B,C)
     * @param rightMapping mapping function to apply to D
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> @NonNull QuadJoiner<A, B, C, D> greaterThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new DefaultQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #filtering(BiPredicate)}.
     *
     * @param filter filter to apply
     * @param <A> type of the first fact in the tuple
     * @param <B> type of the second fact in the tuple
     * @param <C> type of the third fact in the tuple
     * @param <D> type of the fourth fact in the tuple
     */
    public static <A, B, C, D> @NonNull QuadJoiner<A, B, C, D> filtering(@NonNull QuadPredicate<A, B, C, D> filter) {
        return new FilteringQuadJoiner<>(filter);
    }

    /**
     * As defined by {@link #overlapping(Function, Function)}.
     *
     * @param leftStartMapping maps the first, second and third arguments to their interval start point (inclusive)
     * @param leftEndMapping maps the first, second and third arguments to their interval end point (exclusive)
     * @param rightStartMapping maps the fourth argument to its interval start point (inclusive)
     * @param rightEndMapping maps the fourth argument to its interval end point (exclusive)
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <Property_> the type used to define the interval, comparable
     */
    public static <A, B, C, D, Property_ extends Comparable<Property_>> @NonNull QuadJoiner<A, B, C, D> overlapping(
            TriFunction<A, B, C, Property_> leftStartMapping, TriFunction<A, B, C, Property_> leftEndMapping,
            Function<D, Property_> rightStartMapping, Function<D, Property_> rightEndMapping) {
        return Joiners.lessThan(leftStartMapping, rightEndMapping)
                .and(Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    // ************************************************************************
    // PentaJoiner
    // ************************************************************************

    /**
     * As defined by {@link #equal(Function, Function)}
     *
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of the object on the right
     * @param <Property_> the type of the property to compare
     * @param leftMapping mapping function to apply to (A,B,C,D)
     * @param rightMapping mapping function to apply to E
     */
    public static <A, B, C, D, E, Property_> @NonNull PentaJoiner<A, B, C, D, E> equal(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new DefaultPentaJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #lessThan(Function, Function)}
     *
     * @param leftMapping mapping function to apply to (A,B,C,D)
     * @param rightMapping mapping function to apply to E
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> @NonNull PentaJoiner<A, B, C, D, E> lessThan(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new DefaultPentaJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    /**
     * As defined by {@link #lessThanOrEqual(Function, Function)}
     *
     * @param leftMapping mapping function to apply to (A,B,C,D)
     * @param rightMapping mapping function to apply to E
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> @NonNull PentaJoiner<A, B, C, D, E> lessThanOrEqual(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new DefaultPentaJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #greaterThan(Function, Function)}
     *
     * @param leftMapping mapping function to apply to (A,B,C,D)
     * @param rightMapping mapping function to apply to E
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> @NonNull PentaJoiner<A, B, C, D, E> greaterThan(
            QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new DefaultPentaJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    /**
     * As defined by {@link #greaterThanOrEqual(Function, Function)}
     *
     * @param leftMapping mapping function to apply to (A,B,C,D)
     * @param rightMapping mapping function to apply to E
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of object on the right
     * @param <Property_> the type of the property to compare
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> @NonNull PentaJoiner<A, B, C, D, E>
            greaterThanOrEqual(
                    QuadFunction<A, B, C, D, Property_> leftMapping, Function<E, Property_> rightMapping) {
        return new DefaultPentaJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * As defined by {@link #filtering(BiPredicate)}.
     *
     * @param filter filter to apply
     * @param <A> the type of the first object on the left
     * @param <B> the type of the second object on the left
     * @param <C> the type of the third object on the left
     * @param <D> the type of the fourth object on the left
     * @param <E> the type of object on the right
     */
    public static <A, B, C, D, E> @NonNull PentaJoiner<A, B, C, D, E> filtering(@NonNull PentaPredicate<A, B, C, D, E> filter) {
        return new FilteringPentaJoiner<>(filter);
    }

    /**
     * As defined by {@link #overlapping(Function, Function)}.
     *
     * @param leftStartMapping maps the first, second, third and fourth arguments to their interval start point (inclusive)
     * @param leftEndMapping maps the first, second, third and fourth arguments to their interval end point (exclusive)
     * @param rightStartMapping maps the fifth argument to its interval start point (inclusive)
     * @param rightEndMapping maps the fifth argument to its interval end point (exclusive)
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     * @param <D> the type of the fourth argument
     * @param <E> the type of the fifth argument
     * @param <Property_> the type used to define the interval, comparable
     */
    public static <A, B, C, D, E, Property_ extends Comparable<Property_>> @NonNull PentaJoiner<A, B, C, D, E> overlapping(
            QuadFunction<A, B, C, D, Property_> leftStartMapping, QuadFunction<A, B, C, D, Property_> leftEndMapping,
            Function<E, Property_> rightStartMapping, Function<E, Property_> rightEndMapping) {
        return Joiners.lessThan(leftStartMapping, rightEndMapping)
                .and(Joiners.greaterThan(leftEndMapping, rightStartMapping));
    }

    private Joiners() {
    }

}
