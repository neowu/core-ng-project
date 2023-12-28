package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.util.Threads;
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
        executor = new ExecutorImpl(ThreadPools.virtualThreadExecutor("test-"), logManager, 30_000_000_000L);
    }

    @AfterEach
    void cleanup() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1000);
    }

    @Test
    void submit() throws ExecutionException, InterruptedException {
        ActionLog parentAction = logManager.begin("begin", null);
        parentAction.action("parentAction");
        parentAction.trace = Trace.CASCADE;
        parentAction.correlationIds = List.of("correlationId");

        Future<Boolean> future = executor.submit("action", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("parentAction:task:action");
            assertThat(actionLog.trace).isEqualTo(Trace.CASCADE);
            assertThat(actionLog.correlationIds).containsExactly("correlationId");
            return Boolean.TRUE;
        });
        assertThat(future.get()).isEqualTo(true);

        logManager.end("end");
    }

    @Test
    void submitTask() throws ExecutionException, InterruptedException {
        Future<Void> future = executor.submit("action", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("task:action");
            assertThat(actionLog.context).doesNotContainKey("root_action");
            assertThat(actionLog.trace).isEqualTo(Trace.NONE);
        });
        assertThat(future.get()).isNull();
    }

    @Test
    void submitWithDelayedTask() {
        executor.submit("action", () -> {
        }, Duration.ofHours(12));

        assertThat(executor.scheduler).isNotNull();
    }

    @Test
    void scheduleDelayedTask() throws InterruptedException {
        executor.scheduler = ThreadPools.singleThreadScheduler("test-");
        boolean scheduled = executor.scheduleDelayedTask("action", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("task:action");
            assertThat(actionLog.context).doesNotContainKey("root_action");
        }, Duration.ZERO);
        executor.scheduler.shutdown();
        executor.scheduler.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(scheduled).isTrue();
    }

    @Test
    void shutdownWithCanceledTask() {
        executor.scheduler = ThreadPools.singleThreadScheduler("test-");
        boolean scheduled = executor.scheduleDelayedTask("action", () -> {
        }, Duration.ofHours(1));
        executor.shutdown();
        assertThat(scheduled).isTrue();
    }

    @Test
    void submitAfterShutdown() {
        executor.shutdown();
        Future<Object> future = executor.submit("task", () -> null);

        assertThat(future).isNotDone().isCancelled();
        assertThat(future.cancel(true)).isTrue();
        assertThatThrownBy(future::get).isInstanceOf(CancellationException.class);
        assertThatThrownBy(() -> future.get(100, TimeUnit.MILLISECONDS)).isInstanceOf(CancellationException.class);

        executor.submit("task", () -> {
        }, Duration.ZERO);
    }

    @Test
    void scheduleDelayedTaskAfterShutdown() {
        executor.scheduler = ThreadPools.singleThreadScheduler("test-");
        executor.scheduler.shutdown();
        boolean scheduled = executor.scheduleDelayedTask("task", () -> {
        }, Duration.ofHours(1));
        assertThat(scheduled).isFalse();
    }

    @Test
    void awaitTermination() throws InterruptedException {
        executor.submit("task-1", () -> Threads.sleepRoughly(Duration.ofHours(1)));
        executor.submit("task-2", () -> Threads.sleepRoughly(Duration.ofHours(1)));
        executor.shutdown();
        executor.awaitTermination(0);
    }
}
