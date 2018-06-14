package core.framework.test.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MockExecutorTest {
    private MockExecutor executor;

    @BeforeEach
    void createMockExecutor() {
        executor = new MockExecutor();
    }

    @Test
    void get() throws ExecutionException, InterruptedException, TimeoutException {
        Future<Integer> future = executor.submit("action", () -> 12);

        assertThat(future).isDone().isNotCancelled();
        assertThat(future.get(0, TimeUnit.MILLISECONDS)).isEqualTo(12);
    }

    @Test
    void submitTask() {
        Future<Void> future = executor.submit("action", () -> {
        });

        assertThat(future).isDone().isNotCancelled();
    }
}
