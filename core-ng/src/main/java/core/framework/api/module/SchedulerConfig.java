package core.framework.api.module;

import core.framework.api.scheduler.Job;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.DailyTrigger;
import core.framework.impl.scheduler.FixedRateTrigger;
import core.framework.impl.scheduler.MonthlyTrigger;
import core.framework.impl.scheduler.WeeklyTrigger;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public final class SchedulerConfig {
    private final ModuleContext context;

    public SchedulerConfig(ModuleContext context) {
        this.context = context;
    }

    public void fixedRate(String name, Job job, Duration rate) {
        context.scheduler().addTrigger(new FixedRateTrigger(name, job, rate));
    }

    public void dailyAt(String name, Job job, LocalTime time) {
        context.scheduler().addTrigger(new DailyTrigger(name, job, time));
    }

    public void weeklyAt(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        context.scheduler().addTrigger(new WeeklyTrigger(name, job, dayOfWeek, time));
    }

    public void monthlyAt(String name, Job job, int dayOfMonth, LocalTime time) {
        context.scheduler().addTrigger(new MonthlyTrigger(name, job, dayOfMonth, time));
    }
}
