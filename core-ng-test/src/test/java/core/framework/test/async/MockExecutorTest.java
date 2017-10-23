package core.framework.test.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals(12, future.get(0, TimeUnit.MILLISECONDS).intValue());
    }
}
