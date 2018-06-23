package core.framework.impl.async;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author neo
 */
class ThreadPoolsTest {
    @Test
    void awaitTerminationWithNegativeTimeout() {
        ScheduledExecutorService executor = ThreadPools.singleThreadScheduler("test-scheduler-");
        executor.shutdown();
        ThreadPools.awaitTermination(executor, -1, "test-scheduler");
    }
}
