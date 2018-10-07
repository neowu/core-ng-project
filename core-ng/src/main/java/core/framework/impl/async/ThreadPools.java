package core.framework.impl.async;

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
    // provide thread pool with no limit task queue, and start at max pool size num of threads
    // with SynchronousQueue, it will only accept new tasks if there is an idle thread available (that's why Executors.newCachedThreadPool() uses Integer.MAX_VALUE as maximumPoolSize)
    // refer to java.util.concurrent.ThreadPoolExecutor.execute for how it determines to create new thread
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
