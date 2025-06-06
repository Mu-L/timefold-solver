package ai.timefold.solver.core.impl.domain.variable.listener.support;

import ai.timefold.solver.core.api.domain.variable.ListVariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public final class ListVariableChangedNotification<Solution_> extends AbstractNotification
        implements ListVariableNotification<Solution_> {

    private final int fromIndex;
    private final int toIndex;

    ListVariableChangedNotification(Object entity, int fromIndex, int toIndex) {
        super(entity);
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    @Override
    public void triggerBefore(ListVariableListener<Solution_, Object, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        variableListener.beforeListVariableChanged(scoreDirector, entity, fromIndex, toIndex);
    }

    @Override
    public void triggerAfter(ListVariableListener<Solution_, Object, Object> variableListener,
            ScoreDirector<Solution_> scoreDirector) {
        variableListener.afterListVariableChanged(scoreDirector, entity, fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return "ListVariableChangedNotification(" + entity + "[" + fromIndex + ".." + toIndex + "])";
    }
}
