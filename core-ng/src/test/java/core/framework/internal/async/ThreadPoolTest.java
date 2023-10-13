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
class ThreadPoolTest {
    private ExecutorService pool;
    private ExecutorService virtualThreadExecutor;

    @BeforeEach
    void createThreadPool() {
        pool = ThreadPools.cachedThreadPool(1, "test-cached-pool-");
        virtualThreadExecutor = ThreadPools.virtualThreadExecutor("test-virtual-thread-executor-");
    }

    @AfterEach
    void closeThreadPool() {
        pool.shutdown();
        virtualThreadExecutor.shutdown();
    }

    @Test
    void threadName() throws ExecutionException, InterruptedException {
        pool.submit(() -> assertThat(Thread.currentThread().getName()).isEqualTo("test-cached-pool-1"))
            .get();

        List<Future<Object>> futures = virtualThreadExecutor.invokeAll(List.of(
            () -> assertThat(Thread.currentThread().getName()).startsWith("test-virtual-thread-executor-"),
            () -> assertThat(Thread.currentThread().getName()).startsWith("test-virtual-thread-executor-")
        ));
        for (Future<?> future : futures) {
            future.get();
        }
    }
}
