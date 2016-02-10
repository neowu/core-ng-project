package core.framework.impl.module;

import core.framework.api.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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

    public void stop() {
        logger.info("stop background task executor");
        scheduler.shutdown();
    }

    public void scheduleWithFixedDelay(Runnable command, Duration rate) {
        Duration delay = Duration.ofMillis((long) Randoms.number(8000, 15000)); // delay 8s to 15s
        scheduler.scheduleWithFixedDelay(command, delay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static class BackgroundTaskThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("background-task");
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        }
    }
}
