package core.framework.internal.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class VirtualThreadExecutorTest {
    private ExecutorService executor;

    @BeforeEach
    void createExecutor() {
        executor = ThreadPools.virtualThreadExecutor("test-executor-");
    }

    @AfterEach
    void shutdownExecutor() {
        executor.shutdown();
    }

    @Test
    void threadName() throws ExecutionException, InterruptedException {
        List<Future<Object>> futures = executor.invokeAll(List.of(
            () -> assertThat(Thread.currentThread().getName()).startsWith("test-executor-"),
            () -> assertThat(Thread.currentThread().getName()).startsWith("test-executor-")
        ));
        for (Future<?> future : futures) {
            future.get();
        }
    }
}
