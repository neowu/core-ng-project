package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.log.ActionLogContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ExecutorTaskTest {
    @Test
    void action() {
        assertThat(new ExecutorTask<Void>(() -> null, null, context(), null).action())
            .isEqualTo("task:action");

        var parentActionLog = new ActionLog(null, null);
        parentActionLog.action = "parentAction";
        assertThat(new ExecutorTask<Void>(() -> null, null, context(), parentActionLog).action())
            .isEqualTo("parentAction:task:action");

        parentActionLog.context("root_action", "rootAction");
        assertThat(new ExecutorTask<Void>(() -> null, null, context(), parentActionLog).action())
            .isEqualTo("rootAction:task:action");
    }

    @Test
    void callWithException() {
        var task = new ExecutorTask<Void>(() -> {
            throw new Error("test");
        }, new LogManager(), context(), null);
        assertThatThrownBy(task::call)
            .isInstanceOf(TaskException.class)
            .hasMessageContaining("task failed")
            .hasMessageContaining("id=")
            .hasMessageContaining("action=action");
    }

    @Test
    void call() throws Exception {
        var task = new ExecutorTask<>(() -> {
            assertThat(ActionLogContext.get("thread")).hasSize(1);
            return Boolean.TRUE;
        }, new LogManager(), context(), null);
        assertThat(task.call()).isTrue();
    }

    @Test
    void convertToString() {
        assertThat(new ExecutorTask<Void>(() -> null, null, context(), null).toString())
            .isEqualTo("task:action:actionId");
    }

    private ExecutorTask.TaskContext context() {
        return new ExecutorTask.TaskContext("actionId", "action", Instant.now(), 25_000_000_000L, new HashSet<>());
    }
}
