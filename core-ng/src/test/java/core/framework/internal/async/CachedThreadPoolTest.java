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
class CachedThreadPoolTest {
    private ExecutorService pool;
    private ExecutorService unlimitedPool;

    @BeforeEach
    void createThreadPool() {
        pool = ThreadPools.cachedThreadPool(1, "test-cached-pool-");
        unlimitedPool = ThreadPools.cachedThreadPool("test-unlimited-cached-pool-");
    }

    @AfterEach
    void closeThreadPool() {
        pool.shutdown();
        unlimitedPool.shutdown();
    }

    @Test
    void threadName() throws ExecutionException, InterruptedException {
        Future<?> future = pool.submit(() -> assertThat(Thread.currentThread().getName()).isEqualTo("test-cached-pool-1"));
        future.get();
        List<Future<Object>> futures = unlimitedPool.invokeAll(List.of(
            () -> assertThat(Thread.currentThread().getName()).isEqualTo("test-unlimited-cached-pool-1"),
            () -> assertThat(Thread.currentThread().getName()).isEqualTo("test-unlimited-cached-pool-2")
        ));
        for (Future<?> result : futures) {
            result.get();
        }
    }
}
