package core.framework.impl.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CachedThreadPoolTest {
    private ExecutorService pool;

    @BeforeEach
    void createThreadPool() {
        pool = ThreadPools.cachedThreadPool(1, "test-thread-pool-");
    }

    @AfterEach
    void closeThreadPool() {
        pool.shutdown();
    }

    @Test
    void threadName() throws ExecutionException, InterruptedException {
        Future<?> future = pool.submit(() -> assertThat(Thread.currentThread().getName()).isEqualTo("test-thread-pool-1"));
        future.get();
    }
}
