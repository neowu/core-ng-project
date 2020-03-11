package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ExecutorTaskTest {
    @Test
    void action() {
        assertThat(new ExecutorTask<Void>("action", () -> null, null, null).action)
                .isEqualTo("task:action");

        var parentActionLog = new ActionLog(null);
        parentActionLog.action = "parentAction";
        assertThat(new ExecutorTask<Void>("action", () -> null, null, parentActionLog).action)
                .isEqualTo("parentAction:action");

        parentActionLog.context("root_action", "rootAction");
        assertThat(new ExecutorTask<Void>("action", () -> null, null, parentActionLog).action)
                .isEqualTo("rootAction:action");
    }
}
