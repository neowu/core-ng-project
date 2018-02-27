package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.DailyTrigger;
import core.framework.impl.scheduler.FixedRateTrigger;
import core.framework.impl.scheduler.MonthlyTrigger;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.scheduler.SecondlyTrigger;
import core.framework.impl.scheduler.WeeklyTrigger;
import core.framework.impl.web.management.SchedulerController;
import core.framework.scheduler.Job;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public final class SchedulerConfig {
    private final State state;

    SchedulerConfig(ModuleContext context) {
        state = context.config.state("scheduler", State::new);
        if (state.scheduler == null) {
            state.scheduler = createScheduler(context);
        }
    }

    public void timeZone(ZoneId zoneId) {
        if (state.triggerAdded) throw new Error("schedule().timeZone() must be configured before adding trigger");
        if (zoneId == null) throw new Error("zoneId must not be null");
        state.zoneId = zoneId;
    }

    public void fixedRate(String name, Job job, Duration rate) {
        state.scheduler.addTrigger(new FixedRateTrigger(name, job, rate));
        state.triggerAdded = true;
    }

    // run every X seconds and align with the closest exact start of minute
    public void secondly(String name, Job job, int rateInSeconds) {
        state.scheduler.addTrigger(new SecondlyTrigger(name, job, rateInSeconds));
        state.triggerAdded = true;
    }

    public void dailyAt(String name, Job job, LocalTime time) {
        state.scheduler.addTrigger(new DailyTrigger(name, job, time, state.zoneId));
        state.triggerAdded = true;
    }

    public void weeklyAt(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        state.scheduler.addTrigger(new WeeklyTrigger(name, job, dayOfWeek, time, state.zoneId));
        state.triggerAdded = true;
    }

    public void monthlyAt(String name, Job job, int dayOfMonth, LocalTime time) {
        state.scheduler.addTrigger(new MonthlyTrigger(name, job, dayOfMonth, time, state.zoneId));
        state.triggerAdded = true;
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

    public static class State {
        Scheduler scheduler;
        boolean triggerAdded;
        ZoneId zoneId = ZoneId.systemDefault();
    }
}
