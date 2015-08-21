package core.framework.api.module;

import core.framework.api.scheduler.Job;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.DailyTrigger;
import core.framework.impl.scheduler.FixedRateTrigger;

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
}
