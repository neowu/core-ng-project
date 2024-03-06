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
    static {
        // increase parallelism to allow more virtual thread unfriendly tasks to run
        // refer to java.lang.VirtualThread.createDefaultScheduler
        if (System.getProperty("jdk.virtualThreadScheduler.parallelism") == null) {
            int parallelism = Math.max(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 8);
            System.setProperty("jdk.virtualThreadScheduler.parallelism", String.valueOf(parallelism));
        }
    }

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

    // start all virtual thread creation from here to make sure static block above run first
    public static Thread.Builder.OfVirtual virtualThreadBuilder(String prefix) {
        return Thread.ofVirtual().name(prefix, 0);
    }
}
