package core.framework.impl.module;

import core.framework.api.util.Lists;
import core.framework.api.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class BackgroundTaskExecutor {
    private final Logger logger = LoggerFactory.getLogger(BackgroundTaskExecutor.class);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new BackgroundTaskThreadFactory());
    private final List<BackgroundTask> tasks = Lists.newArrayList();

    public void start() {
        for (BackgroundTask task : tasks) {
            Duration delay = Duration.ofMillis((long) Randoms.number(8000, 15000)); // delay 8s to 15s
            scheduler.scheduleWithFixedDelay(task.command, delay.toMillis(), task.rate.toMillis(), TimeUnit.MILLISECONDS);
        }
        tasks.clear();
        logger.info("background task executor started");
    }

    public void stop() {
        logger.info("stop background task executor");
        scheduler.shutdown();
    }

    public void scheduleWithFixedDelay(Runnable command, Duration rate) {
        tasks.add(new BackgroundTask(command, rate));
    }

    private static class BackgroundTaskThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("background-task");
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class BackgroundTask {
        final Runnable command;
        final Duration rate;

        BackgroundTask(Runnable command, Duration rate) {
            this.command = command;
            this.rate = rate;
        }
    }
}
