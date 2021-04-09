package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ExecutorTaskTest {
    @Test
    void action() {
        assertThat(new ExecutorTask<Void>("actionId", "action", Instant.now(), null, null, () -> null).action())
                .isEqualTo("task:action");

        var parentActionLog = new ActionLog(null, null);
        parentActionLog.action = "parentAction";
        assertThat(new ExecutorTask<Void>("actionId", "action", Instant.now(), parentActionLog, null, () -> null).action())
                .isEqualTo("parentAction:task:action");

        parentActionLog.context("root_action", "rootAction");
        assertThat(new ExecutorTask<Void>("actionId", "action", Instant.now(), parentActionLog, null, () -> null).action())
                .isEqualTo("rootAction:task:action");
    }

    @Test
    void callWithException() {
        var task = new ExecutorTask<Void>("actionId", "action", Instant.now(), null, new LogManager(), () -> {
            throw new Error("test");
        });
        assertThatThrownBy(task::call)
                .isInstanceOf(TaskException.class)
                .hasMessageContaining("task failed")
                .hasMessageContaining("id=")
                .hasMessageContaining("action=action");
    }
}
