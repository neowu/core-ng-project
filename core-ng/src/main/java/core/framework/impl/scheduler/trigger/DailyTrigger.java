package core.framework.impl.scheduler.trigger;

import core.framework.api.scheduler.Job;
import core.framework.impl.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class DailyTrigger implements Trigger {
    private final Logger logger = LoggerFactory.getLogger(DailyTrigger.class);

    final LocalTime time;

    public DailyTrigger(LocalTime time) {
        this.time = time;
    }

    @Override
    public void schedule(Scheduler scheduler, String name, Job job) {
        logger.info("scheduled daily job, name={}, atTime={}, job={}", name, time, job.getClass().getCanonicalName());
        LocalTime now = LocalTime.now();
        Duration delay = delayToNextScheduledTime(time, now);
        scheduler.schedule(name, job, delay, Duration.ofDays(1));
    }

    Duration delayToNextScheduledTime(LocalTime time, LocalTime now) {
        Duration delay = Duration.between(now, time);
        long delayInSeconds = delay.getSeconds();
        if (delayInSeconds < 0) return delay.plus(Duration.ofDays(1));
        return delay;
    }
}
