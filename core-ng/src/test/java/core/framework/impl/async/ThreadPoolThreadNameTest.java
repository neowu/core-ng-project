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
class ThreadPoolThreadNameTest {
    private ExecutorService threadPool;

    @BeforeEach
    void createThreadPool() {
        threadPool = ThreadPools.cachedThreadPool(1, "test-thread-pool-");
    }

    @AfterEach
    void closeThreadPool() {
        threadPool.shutdown();
    }

    @Test
    void threadName() throws ExecutionException, InterruptedException {
        Future<?> future = threadPool.submit(() -> assertThat(Thread.currentThread().getName()).isEqualTo("test-thread-pool-1"));
        future.get();
    }
}
