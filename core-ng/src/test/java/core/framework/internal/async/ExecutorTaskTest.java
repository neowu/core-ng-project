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
        assertThat(new ExecutorTask<Void>("action", () -> null, null, null, Instant.now()).action())
                .isEqualTo("task:action");

        var parentActionLog = new ActionLog(null);
        parentActionLog.action = "parentAction";
        assertThat(new ExecutorTask<Void>("action", () -> null, null, parentActionLog, Instant.now()).action())
                .isEqualTo("parentAction:action");

        parentActionLog.context("root_action", "rootAction");
        assertThat(new ExecutorTask<Void>("action", () -> null, null, parentActionLog, Instant.now()).action())
                .isEqualTo("rootAction:action");
    }

    @Test
    void callWithException() {
        var task = new ExecutorTask<Void>("action", () -> {
            throw new Error("test");
        }, new LogManager(), null, Instant.now());
        assertThatThrownBy(task::call)
                .isInstanceOf(TaskException.class)
                .hasMessageContaining("task failed")
                .hasMessageContaining("id=")
                .hasMessageContaining("action=action");
    }
}
