package core.framework.internal.async;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ExecutorImplTest {
    private LogManager logManager;
    private ExecutorImpl executor;

    @BeforeEach
    void createExecutorImpl() {
        logManager = new LogManager();
        executor = new ExecutorImpl(1, "test", logManager);

        ActionLog actionLog = logManager.begin("begin");
        actionLog.action("parentAction");
        actionLog.trace = true;
        actionLog.correlationIds = List.of("correlationId");
    }

    @AfterEach
    void cleanup() {
        logManager.end("end");
        executor.shutdown();
    }

    @Test
    void submit() throws ExecutionException, InterruptedException {
        Future<Boolean> future = executor.submit("action", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("parentAction:action");
            assertThat(actionLog.trace).isEqualTo(true);
            assertThat(actionLog.correlationIds).containsExactly("correlationId");
            return Boolean.TRUE;
        });
        assertThat(future.get()).isEqualTo(true);
    }

    @Test
    void submitTask() throws ExecutionException, InterruptedException {
        Future<Void> future = executor.submit("task", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("parentAction:task");
            assertThat(actionLog.trace).isEqualTo(true);
            assertThat(actionLog.correlationIds).containsExactly("correlationId");
        });
        assertThat(future.get()).isNull();
    }

    @Test
    void taskAction() {
        assertThat(executor.taskAction("task", "parentAction")).isEqualTo("parentAction:task");
        assertThat(executor.taskAction("task", "parentAction:task")).isEqualTo("parentAction:task");
    }

    @Test
    void submitAfterShutdown() {
        executor.shutdown();
        Future<Object> future = executor.submit("task", () -> null);
        assertThat(future).isNotDone().isCancelled();

        assertThatThrownBy(future::get).isInstanceOf(CancellationException.class);
        assertThatThrownBy(() -> future.get(100, TimeUnit.MILLISECONDS)).isInstanceOf(CancellationException.class);

        executor.submit("task", () -> {
        }, Duration.ZERO);
    }
}
