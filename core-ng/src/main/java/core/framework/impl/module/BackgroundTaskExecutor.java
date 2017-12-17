package core.framework.impl.module;

import core.framework.util.Lists;
import core.framework.util.Randoms;
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
            Duration delay = Duration.ofMillis((long) Randoms.number(5000, 10000)); // delay 5s to 10s
            scheduler.scheduleWithFixedDelay(task, delay.toMillis(), task.rate.toMillis(), TimeUnit.MILLISECONDS);
        }
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
            Thread thread = new Thread(runnable, "background-task");
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class BackgroundTask implements Runnable {
        final Duration rate;
        private final Logger logger = LoggerFactory.getLogger(BackgroundTask.class);
        private final Runnable command;

        BackgroundTask(Runnable command, Duration rate) {
            this.command = command;
            this.rate = rate;
        }

        @Override
        public void run() {
            try {
                command.run();
            } catch (Throwable e) {
                logger.warn("failed to run background task, error={}", e.getMessage(), e);
            }
        }
    }
}
