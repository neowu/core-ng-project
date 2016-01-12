package core.framework.impl.scheduler;

import core.framework.api.scheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author tminglei
 */
public class WeeklyTrigger extends Trigger {
    private final Logger logger = LoggerFactory.getLogger(MonthlyTrigger.class);

    private final int dayOfWeek;
    private final LocalTime time;

    public WeeklyTrigger(String name, Job job, int dayOfWeek, LocalTime time) {
        super(name, job);
        this.dayOfWeek = dayOfWeek;
        this.time = time;

        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("ILLEGAL dayOfWeek " + dayOfWeek + "!!!");
        }
    }

    @Override
    void schedule(Scheduler scheduler) {
        logger.info("scheduled weekly job, name={}, atTime={}, job={}", name, atTimeInfo(), job.getClass().getCanonicalName());
        scheduler.schedule(name, job, initialDelay(), rate());
    }

    Duration initialDelay() {
        int currDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        LocalDateTime thisTime = LocalDateTime.of(LocalDate.now(), time).plusDays(dayOfWeek - currDayOfWeek);

        Duration delay = Duration.between(LocalDateTime.now(), thisTime);
        if (delay.isNegative()) {
            LocalDateTime nextTime = thisTime.plusDays(7);
            return Duration.between(LocalDateTime.now(), nextTime);
        }
        return delay;
    }

    Duration rate() {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, now.plusDays(7));
    }

    @Override
    public String scheduleInfo() {
        return "weekly@" + atTimeInfo();
    }

    private String atTimeInfo() {
        return dayOfWeek + " " + time;
    }
}
