package app;

import app.job.DemoJob;
import core.framework.api.Module;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class JobModule extends Module {
    @Override
    protected void initialize() {
        DemoJob job = bind(DemoJob.class);
        schedule().fixedRate("fixed-rate-job", job, Duration.ofSeconds(15));
        schedule().dailyAt("daily-job", job, LocalTime.of(16, 3));
        schedule().weeklyAt("weekly-job", job, DayOfWeek.WEDNESDAY, LocalTime.of(16, 3));
        schedule().monthlyAt("monthly-job", job, 13, LocalTime.of(16, 3));
    }
}
