package core.framework.impl.async;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ExecutorImplTest {
    private LogManager logManager;
    private ExecutorImpl executor;

    @BeforeEach
    void createExecutorImpl() {
        logManager = mock(LogManager.class);
        ExecutorService executorService = mock(ExecutorService.class);
        when(executorService.submit((Callable<?>) any(Callable.class))).then(invocation -> {
            ((Callable<?>) invocation.getArgument(0)).call();
            return null;
        });
        executor = new ExecutorImpl(executorService, logManager, "test");
    }

    @Test
    void submit() {
        ActionLog parentActionLog = new ActionLog(null, null);
        parentActionLog.action("parentAction");
        parentActionLog.refId("refId");
        parentActionLog.trace = true;
        when(logManager.currentActionLog()).thenReturn(parentActionLog);

        ActionLog taskActionLog = new ActionLog(null, null);
        when(logManager.begin(anyString())).thenReturn(taskActionLog);

        executor.submit("action", () -> true);
        assertThat(taskActionLog.action).isEqualTo("parentAction:action");
        assertThat(taskActionLog.trace).isEqualTo(true);
        assertThat(taskActionLog.refId()).isEqualTo("refId");
    }

    @Test
    void taskAction() {
        assertThat(executor.taskAction("task", "parentAction")).isEqualTo("parentAction:task");
        assertThat(executor.taskAction("task", "parentAction:task")).isEqualTo("parentAction:task");
    }
}
