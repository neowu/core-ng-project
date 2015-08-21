package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class DailyTrigger extends Trigger {
    private final Logger logger = LoggerFactory.getLogger(DailyTrigger.class);

    final LocalTime time;

    public DailyTrigger(String name, Job job, LocalTime time) {
        super(name, job);
        this.time = time;
    }

    Duration delayToNextScheduledTime(LocalTime time, LocalTime now) {
        Duration delay = Duration.between(now, time);
        long delayInSeconds = delay.getSeconds();
        if (delayInSeconds < 0) return delay.plus(Duration.ofDays(1));
        return delay;
    }

    @Override
    void schedule(Scheduler scheduler) {
        logger.info("scheduled daily job, name={}, atTime={}, job={}", name, time, job.getClass().getCanonicalName());
        LocalTime now = LocalTime.now();
        Duration delay = delayToNextScheduledTime(time, now);
        scheduler.schedule(name, job, delay, Duration.ofDays(1));
    }

    @Override
    public String scheduleInfo() {
        return "daily@" + time;
    }
}
