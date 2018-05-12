package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.DailyTrigger;
import core.framework.impl.scheduler.MonthlyTrigger;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.scheduler.WeeklyTrigger;
import core.framework.impl.web.management.SchedulerController;
import core.framework.scheduler.Job;
import core.framework.scheduler.Trigger;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public final class SchedulerConfig {
    private final Scheduler scheduler;
    private boolean triggerAdded;

    SchedulerConfig(ModuleContext context) {
        scheduler = createScheduler(context);
    }

    public void timeZone(ZoneId zoneId) {
        if (triggerAdded) throw new Error("schedule().timeZone() must be configured before adding trigger");
        if (zoneId == null) throw new Error("zoneId must not be null");
        scheduler.clock = Clock.system(zoneId);
    }

    public void fixedRate(String name, Job job, Duration rate) {
        scheduler.addFixedRateTask(name, job, rate);
        triggerAdded = true;
    }

    public void dailyAt(String name, Job job, LocalTime time) {
        trigger(name, job, new DailyTrigger(time));
    }

    public void weeklyAt(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        trigger(name, job, new WeeklyTrigger(dayOfWeek, time));
    }

    public void monthlyAt(String name, Job job, int dayOfMonth, LocalTime time) {
        trigger(name, job, new MonthlyTrigger(dayOfMonth, time));
    }

    public void trigger(String name, Job job, Trigger trigger) {
        scheduler.addTriggerTask(name, job, trigger);
        triggerAdded = true;
    }

    private Scheduler createScheduler(ModuleContext context) {
        Scheduler scheduler = new Scheduler(context.logManager);
        context.startupHook.add(scheduler::start);
        context.shutdownHook.add(scheduler::stop);

        SchedulerController schedulerController = new SchedulerController(scheduler);
        context.route(HTTPMethod.GET, "/_sys/job", schedulerController::jobs, true);
        context.route(HTTPMethod.POST, "/_sys/job/:job", schedulerController::triggerJob, true);

        return scheduler;
    }
}
