package core.framework.internal.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author neo
 */
public final class ThreadPools {
    public static ExecutorService virtualThreadExecutor(String prefix) {
        ThreadFactory factory = Thread.ofVirtual().name(prefix, 0).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    public static ScheduledExecutorService singleThreadScheduler(String prefix) {
        ThreadFactory factory = Thread.ofPlatform().name(prefix, 0).factory();
        var scheduler = new ScheduledThreadPoolExecutor(1, factory);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return scheduler;
    }
}
