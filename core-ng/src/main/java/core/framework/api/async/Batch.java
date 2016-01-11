package core.framework.api.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * @author neo
 */
public final class Batch<T> implements AutoCloseable {
    private final String action;
    private final Executor executor;
    private final Semaphore semaphore;
    private final int maxConcurrentHandlers;

    public Batch(String action, Executor executor) {
        this.action = action;
        this.executor = executor;
        maxConcurrentHandlers = Runtime.getRuntime().availableProcessors() * 4;
        semaphore = new Semaphore(maxConcurrentHandlers);
    }

    public Future<T> submit(Callable<T> task) {
        // do not log intentionally, so in batch pattern where one batch submit task for each item, the batch process won't reach max trace log line limit.
        semaphore.acquireUninterruptibly(); // only be interrupted by shutdown
        return executor.submit(action, () -> {
            try {
                return task.call();
            } finally {
                semaphore.release();
            }
        });
    }

    @Override
    public void close() {
        semaphore.acquireUninterruptibly(maxConcurrentHandlers);    // only be interrupted by shutdown
    }
}
