package core.framework.internal.async;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ExecutorImplStandaloneTest {
    private ExecutorImpl executor;

    @BeforeEach
    void createExecutorImpl() {
        executor = new ExecutorImpl(1, null, new LogManager());
    }

    @AfterEach
    void cleanup() {
        executor.shutdown();
    }

    @Test
    void submit() throws ExecutionException, InterruptedException {
        Future<Boolean> future = executor.submit("action", () -> {
            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            assertThat(actionLog.action).isEqualTo("task:action");
            assertThat(actionLog.context).doesNotContainKey("root_action");
            assertThat(actionLog.trace).isFalse();
            assertThat(actionLog.refIds).isNull();
            assertThat(actionLog.correlationIds).isNull();
            return Boolean.TRUE;
        });
        assertThat(future.get()).isEqualTo(true);
    }
}
