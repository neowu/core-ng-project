package core.framework.impl.module;

import core.framework.impl.async.ThreadPools;
import core.framework.util.Lists;
import core.framework.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class BackgroundTaskExecutor {
    private final Logger logger = LoggerFactory.getLogger(BackgroundTaskExecutor.class);

    private final ScheduledExecutorService scheduler = ThreadPools.singleThreadScheduler("background-task-");
    private final List<BackgroundTask> tasks = Lists.newArrayList();

    public void start() {
        for (BackgroundTask task : tasks) {
            Duration delay = Duration.ofMillis((long) Randoms.nextDouble(5000, 10000)); // delay 5s to 10s
            scheduler.scheduleWithFixedDelay(task, delay.toMillis(), task.rate.toMillis(), TimeUnit.MILLISECONDS);
        }
        logger.info("background task executor started");
    }

    public void stop() {
        logger.info("stop background task executor");
        scheduler.shutdown();
        try {
            boolean success = scheduler.awaitTermination(10, TimeUnit.SECONDS);
            if (!success) logger.warn("failed to terminate background task executor");
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void scheduleWithFixedDelay(Runnable command, Duration rate) {
        tasks.add(new BackgroundTask(command, rate));
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
                logger.error("failed to run background task, error={}", e.getMessage(), e);
            }
        }
    }
}
