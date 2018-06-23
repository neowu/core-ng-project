package core.framework.impl.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public final class ThreadPools {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPools.class);

    public static ExecutorService cachedThreadPool(int poolSize, String prefix) {
        var threadPool = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryImpl(prefix));
        threadPool.allowCoreThreadTimeOut(true);
        return threadPool;
    }

    public static ScheduledExecutorService singleThreadScheduler(String prefix) {
        var scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactoryImpl(prefix));
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return scheduler;
    }

    public static void awaitTermination(ExecutorService executor, long timeoutInMs, String name) {
        try {
            boolean success = executor.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
            if (!success) LOGGER.warn("failed to terminate {}", name);
            else LOGGER.info("{} stopped", name);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    static class ThreadFactoryImpl implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(1);
        private final String prefix;

        ThreadFactoryImpl(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, prefix + count.getAndIncrement());
        }
    }
}
