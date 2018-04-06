package core.framework.impl.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ThreadPoolsTest {
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
        Future<?> future = threadPool.submit(() -> assertEquals("test-thread-pool-1", Thread.currentThread().getName()));
        future.get();
    }
}
