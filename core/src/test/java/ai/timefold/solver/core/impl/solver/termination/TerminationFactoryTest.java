package ai.timefold.solver.core.impl.solver.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.timefold.solver.core.config.solver.termination.DiminishedReturnsTerminationConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationCompositionStyle;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import ai.timefold.solver.core.impl.heuristic.HeuristicConfigPolicy;
import ai.timefold.solver.core.impl.score.buildin.HardSoftScoreDefinition;
import ai.timefold.solver.core.impl.score.buildin.SimpleScoreDefinition;
import ai.timefold.solver.core.testdomain.TestdataSolution;

import org.junit.jupiter.api.Test;

class TerminationFactoryTest {

    @Test
    void spentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSpentLimit(Duration.ofMillis(5L)
                .plusSeconds(4L)
                .plusMinutes(3L)
                .plusHours(2L)
                .plusDays(1L));
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(TimeMillisSpentTermination.class);
        assertThat(((TimeMillisSpentTermination<?>) termination).getTimeMillisSpentLimit()).isEqualTo(93784005L);
    }

    @Test
    void spentLimitWithoutJavaTime() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMillisecondsSpentLimit(5L);
        terminationConfig.setSecondsSpentLimit(4L);
        terminationConfig.setMinutesSpentLimit(3L);
        terminationConfig.setHoursSpentLimit(2L);
        terminationConfig.setDaysSpentLimit(1L);
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(TimeMillisSpentTermination.class);
        assertThat(((TimeMillisSpentTermination<?>) termination).getTimeMillisSpentLimit()).isEqualTo(93784005L);
    }

    @Test
    void unimprovedSpentLimit() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setUnimprovedSpentLimit(Duration.ofMillis(5L)
                .plusSeconds(4L)
                .plusMinutes(3L)
                .plusHours(2L)
                .plusDays(1L));
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(UnimprovedTimeMillisSpentTermination.class);
        assertThat(((UnimprovedTimeMillisSpentTermination<?>) termination).getUnimprovedTimeMillisSpentLimit())
                .isEqualTo(93784005L);
    }

    @Test
    void unimprovedSpentLimitWithoutJavaTime() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setUnimprovedMillisecondsSpentLimit(5L);
        terminationConfig.setUnimprovedSecondsSpentLimit(4L);
        terminationConfig.setUnimprovedMinutesSpentLimit(3L);
        terminationConfig.setUnimprovedHoursSpentLimit(2L);
        terminationConfig.setUnimprovedDaysSpentLimit(1L);
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(UnimprovedTimeMillisSpentTermination.class);
        assertThat(((UnimprovedTimeMillisSpentTermination<?>) termination).getUnimprovedTimeMillisSpentLimit())
                .isEqualTo(93784005L);
    }

    @Test
    void buildTerminationFromList_single() {
        Termination<TestdataSolution> termination = new TimeMillisSpentTermination<>(1000L);
        assertThat(TerminationFactory.<TestdataSolution> create(new TerminationConfig())
                .buildTerminationFromList(Collections.singletonList(termination))).isSameAs(termination);
    }

    @Test
    void buildTerminationFromList_withAndComposition() {
        Termination<TestdataSolution> timeBasedTermination = new TimeMillisSpentTermination<>(1000L);
        Termination<TestdataSolution> stepCountTermination = new StepCountTermination<>(5);
        List<Termination<TestdataSolution>> terminationList = new ArrayList<>();
        terminationList.add(timeBasedTermination);
        terminationList.add(stepCountTermination);

        Termination<TestdataSolution> resultingTermination =
                TerminationFactory.<TestdataSolution> create(new TerminationConfig()
                        .withTerminationCompositionStyle(TerminationCompositionStyle.AND))
                        .buildTerminationFromList(terminationList);
        assertThat(resultingTermination).isExactlyInstanceOf(AndCompositeTermination.class);
        AndCompositeTermination<TestdataSolution> andCompositeTermination =
                (AndCompositeTermination<TestdataSolution>) resultingTermination;
        assertThat(andCompositeTermination.terminationList).containsExactly(timeBasedTermination, stepCountTermination);
    }

    @Test
    void buildTerminationFromList_withDefaultComposition() {
        Termination<TestdataSolution> timeBasedTermination = new TimeMillisSpentTermination<>(1000L);
        Termination<TestdataSolution> stepCountTermination = new StepCountTermination<>(5);
        List<Termination<TestdataSolution>> terminationList = new ArrayList<>();
        terminationList.add(timeBasedTermination);
        terminationList.add(stepCountTermination);

        Termination<TestdataSolution> resultingTermination =
                TerminationFactory.<TestdataSolution> create(new TerminationConfig())
                        .buildTerminationFromList(terminationList);
        assertThat(resultingTermination).isExactlyInstanceOf(OrCompositeTermination.class);
        OrCompositeTermination<TestdataSolution> andCompositeTermination =
                (OrCompositeTermination<TestdataSolution>) resultingTermination;
        assertThat(andCompositeTermination.terminationList).containsExactly(timeBasedTermination, stepCountTermination);
    }

    @Test
    void buildInnerTermination() {
        TerminationConfig innerTerminationConfig = new TerminationConfig().withSecondsSpentLimit(1L);
        TerminationConfig outerTerminationConfig = new TerminationConfig()
                .withTerminationConfigList(Collections.singletonList(innerTerminationConfig));
        List<Termination<?>> terminationList =
                TerminationFactory.create(outerTerminationConfig).buildInnerTermination(mock(HeuristicConfigPolicy.class));
        assertThat(terminationList)
                .hasSize(1)
                .hasOnlyElementsOfType(TimeMillisSpentTermination.class);
        TimeMillisSpentTermination<?> timeMillisSpentTermination = (TimeMillisSpentTermination<?>) terminationList.get(0);
        assertThat(timeMillisSpentTermination.getTimeMillisSpentLimit()).isEqualTo(1000L);
    }

    @Test
    void buildTimeBasedTermination_withScoreDifferenceThreshold() {
        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy = mock(HeuristicConfigPolicy.class);
        when(heuristicConfigPolicy.getScoreDefinition()).thenReturn(new HardSoftScoreDefinition());

        TerminationConfig terminationConfig = new TerminationConfig()
                .withMillisecondsSpentLimit(1000L)
                .withUnimprovedMillisecondsSpentLimit(1000L)
                .withUnimprovedScoreDifferenceThreshold("1hard/0soft");
        List<Termination<TestdataSolution>> terminationList =
                TerminationFactory.<TestdataSolution> create(terminationConfig)
                        .buildTimeBasedTermination(heuristicConfigPolicy);
        assertThat(terminationList).hasOnlyElementsOfTypes(TimeMillisSpentTermination.class,
                UnimprovedTimeMillisSpentScoreDifferenceThresholdTermination.class);
    }

    @Test
    void buildTimeBasedTermination_withoutScoreDifferenceThreshold() {
        TerminationConfig terminationConfig = new TerminationConfig()
                .withMillisecondsSpentLimit(1000L)
                .withUnimprovedMillisecondsSpentLimit(1000L);
        List<Termination<TestdataSolution>> terminationList =
                TerminationFactory.<TestdataSolution> create(terminationConfig)
                        .buildTimeBasedTermination(mock(HeuristicConfigPolicy.class));
        assertThat(terminationList).hasOnlyElementsOfTypes(TimeMillisSpentTermination.class,
                UnimprovedTimeMillisSpentTermination.class);
    }

    @Test
    void buildWithMoveCount() {
        TerminationConfig terminationConfig = new TerminationConfig()
                .withMoveCountLimit(1L);
        List<Termination<TestdataSolution>> terminationList =
                TerminationFactory.<TestdataSolution> create(terminationConfig)
                        .buildTimeBasedTermination(mock(HeuristicConfigPolicy.class));
        assertThat(terminationList).hasOnlyElementsOfTypes(MoveCountTermination.class);
    }

    @Test
    void scoreDifferenceThreshold_mustBeUsedWithUnimprovedTimeSpent() {
        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy = mock(HeuristicConfigPolicy.class);

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.withUnimprovedScoreDifferenceThreshold("1hard/0soft");

        TerminationFactory<TestdataSolution> terminationFactory = TerminationFactory.create(terminationConfig);
        assertThatIllegalStateException()
                .isThrownBy(() -> terminationFactory.buildTimeBasedTermination(heuristicConfigPolicy))
                .withMessageContaining("can only be used if an unimproved*SpentLimit");
    }

    @Test
    void bestScoreFeasible_requiresAtLeastOneFeasibleLevel() {
        HeuristicConfigPolicy<TestdataSolution> heuristicConfigPolicy = mock(HeuristicConfigPolicy.class);
        when(heuristicConfigPolicy.getScoreDefinition()).thenReturn(new SimpleScoreDefinition());
        TerminationConfig terminationConfig = new TerminationConfig().withBestScoreFeasible(true);

        TerminationFactory<TestdataSolution> terminationFactory = TerminationFactory.create(terminationConfig);
        assertThatIllegalStateException()
                .isThrownBy(() -> terminationFactory.buildTermination(heuristicConfigPolicy))
                .withMessageContaining("can only be used with a score type that has at least 1 feasible level");
    }

    @Test
    void diminishedReturns_default() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setDiminishedReturnsConfig(new DiminishedReturnsTerminationConfig());
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(DiminishedReturnsTermination.class);
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getSlidingWindowNanos)
                .isEqualTo(Duration.ofSeconds(30).toNanos());
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getMinimumImprovementRatio)
                .isEqualTo(0.0001);
    }

    @Test
    void diminishedReturns_custom() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setDiminishedReturnsConfig(new DiminishedReturnsTerminationConfig()
                .withSlidingWindowMilliseconds(1L)
                .withSlidingWindowSeconds(2L)
                .withSlidingWindowMinutes(3L)
                .withSlidingWindowHours(4L)
                .withSlidingWindowDays(5L)
                .withMinimumImprovementRatio(0.5));
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(DiminishedReturnsTermination.class);
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getSlidingWindowNanos)
                .isEqualTo(Duration.ofMillis(1)
                        .plusSeconds(2)
                        .plusMinutes(3)
                        .plusHours(4)
                        .plusDays(5).toNanos());
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getMinimumImprovementRatio)
                .isEqualTo(0.5);
    }

    @Test
    void diminishedReturns_customDuration() {
        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setDiminishedReturnsConfig(new DiminishedReturnsTerminationConfig()
                .withSlidingWindowDuration(Duration.ofMinutes(123))
                .withMinimumImprovementRatio(0.5));
        Termination<?> termination = TerminationFactory.create(terminationConfig)
                .buildTermination(mock(HeuristicConfigPolicy.class));
        assertThat(termination)
                .isInstanceOf(DiminishedReturnsTermination.class);
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getSlidingWindowNanos)
                .isEqualTo(Duration.ofMinutes(123).toNanos());
        assertThat((DiminishedReturnsTermination<?, ?>) termination)
                .extracting(DiminishedReturnsTermination::getMinimumImprovementRatio)
                .isEqualTo(0.5);
    }
}
