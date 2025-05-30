package ai.timefold.solver.core.api.solver;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.event.SolverEventListener;
import ai.timefold.solver.core.impl.solver.termination.Termination;

import org.jspecify.annotations.NonNull;

/**
 * A Solver solves a planning problem and returns the best solution found.
 * It's recommended to create a new Solver instance for each dataset.
 * <p>
 * To create a Solver, use {@link SolverFactory#buildSolver()}.
 * To solve a planning problem, call {@link #solve(Object)}.
 * To solve a planning problem without blocking the current thread, use {@link SolverManager} instead.
 * <p>
 * These methods are not thread-safe and should be called from the same thread,
 * except for the methods that are explicitly marked as thread-safe.
 * Note that despite that {@link #solve} is not thread-safe for clients of this class,
 * that method is free to do multithreading inside itself.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public interface Solver<Solution_> {

    /**
     * Solves the planning problem and returns the best solution encountered
     * (which might or might not be optimal, feasible or even initialized).
     * <p>
     * It can take seconds, minutes, even hours or days before this method returns,
     * depending on the termination configuration.
     * To terminate a {@link Solver} early, call {@link #terminateEarly()}.
     *
     * @param problem a {@link PlanningSolution}, usually its planning variables are uninitialized
     * @return can return the original, uninitialized {@link PlanningSolution} with a null {@link Score}.
     * @see #terminateEarly()
     */
    @NonNull
    Solution_ solve(@NonNull Solution_ problem);

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * This method returns immediately, but it takes an undetermined time
     * for the {@link #solve} to actually return.
     * <p>
     * If the solver is running in daemon mode, this is the only way to terminate it normally.
     * <p>
     * This method is thread-safe.
     * It can only be called from a different thread
     * because the original thread is still calling {@link #solve(Object)}.
     *
     * @return true if successful, false if was already terminating or terminated
     * @see #isTerminateEarly()
     * @see Future#cancel(boolean)
     */
    boolean terminateEarly();

    /**
     * This method is thread-safe.
     *
     * @return true if the {@link #solve} method is still running.
     */
    boolean isSolving();

    /**
     * This method is thread-safe.
     *
     * @return true if terminateEarly has been called since the {@link Solver} started.
     * @see Future#isCancelled()
     */
    boolean isTerminateEarly();

    /**
     * Schedules a {@link ProblemChange} to be processed.
     * <p>
     * As a side effect, this restarts the {@link Solver}, effectively resetting all {@link Termination}s,
     * but not {@link #terminateEarly()}.
     * <p>
     * This method is thread-safe.
     * Follows specifications of {@link BlockingQueue#add(Object)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     * <p>
     * To learn more about problem change semantics, please refer to the {@link ProblemChange} Javadoc.
     *
     * @see ProblemChange Learn more about problem change semantics.
     * @see #addProblemChanges(List) Submit multiple problem changes at once.
     */
    void addProblemChange(@NonNull ProblemChange<Solution_> problemChange);

    /**
     * Schedules multiple {@link ProblemChange}s to be processed.
     * <p>
     * As a side effect, this restarts the {@link Solver}, effectively resetting all {@link Termination}s,
     * but not {@link #terminateEarly()}.
     * <p>
     * This method is thread-safe.
     * Follows specifications of {@link BlockingQueue#add(Object)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     * <p>
     * To learn more about problem change semantics, please refer to the {@link ProblemChange} Javadoc.
     *
     * @see ProblemChange Learn more about problem change semantics.
     */
    void addProblemChanges(@NonNull List<ProblemChange<Solution_>> problemChangeList);

    /**
     * Checks if all scheduled {@link ProblemChange}s have been processed.
     * <p>
     * This method is thread-safe.
     *
     * @return true if there are no {@link ProblemChange}s left to do
     */
    boolean isEveryProblemChangeProcessed();

    /**
     * This method is deprecated.
     * Schedules a {@link ProblemFactChange} to be processed.
     * <p>
     * As a side-effect, this restarts the {@link Solver}, effectively resetting all terminations,
     * but not {@link #terminateEarly()}.
     * <p>
     * This method is thread-safe.
     * Follows specifications of {@link BlockingQueue#add(Object)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     *
     * @deprecated Prefer {@link #addProblemChange(ProblemChange)}.
     * @return true (as specified by {@link Collection#add})
     * @see #addProblemChanges(List)
     */
    @Deprecated(forRemoval = true)
    boolean addProblemFactChange(@NonNull ProblemFactChange<Solution_> problemFactChange);

    /**
     * This method is deprecated.
     * Schedules multiple {@link ProblemFactChange}s to be processed.
     * <p>
     * As a side-effect, this restarts the {@link Solver}, effectively resetting all terminations,
     * but not {@link #terminateEarly()}.
     * <p>
     * This method is thread-safe.
     * Follows specifications of {@link BlockingQueue#addAll(Collection)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     *
     * @deprecated Prefer {@link #addProblemChanges(List)}.
     * @return true (as specified by {@link Collection#add})
     * @see #addProblemChange(ProblemChange)
     */
    @Deprecated(forRemoval = true)
    boolean addProblemFactChanges(@NonNull List<ProblemFactChange<Solution_>> problemFactChangeList);

    /**
     * This method is deprecated.
     * Checks if all scheduled {@link ProblemFactChange}s have been processed.
     * <p>
     * This method is thread-safe.
     *
     * @deprecated Prefer {@link #isEveryProblemChangeProcessed()}.
     * @return true if there are no {@link ProblemFactChange}s left to do
     */
    @Deprecated(forRemoval = true)
    boolean isEveryProblemFactChangeProcessed();

    void addEventListener(@NonNull SolverEventListener<Solution_> eventListener);

    void removeEventListener(@NonNull SolverEventListener<Solution_> eventListener);

}
