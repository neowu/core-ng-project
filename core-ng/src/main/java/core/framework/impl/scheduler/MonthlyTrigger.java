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
public class MonthlyTrigger extends Trigger {
    private final Logger logger = LoggerFactory.getLogger(MonthlyTrigger.class);

    private final int dayOfMonth;
    private final LocalTime time;

    public MonthlyTrigger(String name, Job job, int dayOfMonth, LocalTime time) {
        super(name, job);
        this.dayOfMonth = dayOfMonth;
        this.time = time;

        if (dayOfMonth < 1 || dayOfMonth > 28) {
            throw new IllegalArgumentException("UNSUPPORTED dayOfMonth " + dayOfMonth + "!!!");
        }
    }

    @Override
    void schedule(Scheduler scheduler) {
        logger.info("scheduled monthly job, name={}, atTime={}, job={}", name, atTimeInfo(), job.getClass().getCanonicalName());
        scheduler.schedule(name, job, initialDelay(), () -> nextDelay());
    }

    Duration initialDelay() {
        LocalDateTime thisTime = LocalDateTime.of(LocalDate.now(), time).withDayOfMonth(dayOfMonth);

        Duration delay = Duration.between(LocalDateTime.now(), thisTime);
        if (delay.isNegative()) {
            LocalDateTime nextTime = thisTime.plusMonths(1);
            return Duration.between(LocalDateTime.now(), nextTime);
        }
        return delay;
    }

    Duration nextDelay() {
        return Duration.between(LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
    }

    @Override
    public String scheduleInfo() {
        return "monthly@" + atTimeInfo();
    }

    private String atTimeInfo() {
        return dayOfMonth + " " + time;
    }
}
