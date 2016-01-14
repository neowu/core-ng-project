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
        LocalTime now = LocalTime.now().plusSeconds(10);
        schedule().dailyAt("daily-job", job, now);
        schedule().weeklyAt("weekly-job", job, DayOfWeek.WEDNESDAY, now);
        schedule().monthlyAt("monthly-job", job, 13, now);
    }
}
