package core.framework.api.module;

import core.framework.api.http.HTTPMethod;
import core.framework.api.scheduler.Job;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.DailyTrigger;
import core.framework.impl.scheduler.FixedRateTrigger;
import core.framework.impl.scheduler.MonthlyTrigger;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.scheduler.WeeklyTrigger;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.management.SchedulerController;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public final class SchedulerConfig {
    private final SchedulerConfigState state;

    public SchedulerConfig(ModuleContext context) {
        state = context.config.scheduler();
        if (state.scheduler == null) {
            state.scheduler = createScheduler(context);
        }
    }

    public void fixedRate(String name, Job job, Duration rate) {
        state.scheduler.addTrigger(new FixedRateTrigger(name, job, rate));
    }

    public void dailyAt(String name, Job job, LocalTime time) {
        state.scheduler.addTrigger(new DailyTrigger(name, job, time));
    }

    public void weeklyAt(String name, Job job, DayOfWeek dayOfWeek, LocalTime time) {
        state.scheduler.addTrigger(new WeeklyTrigger(name, job, dayOfWeek, time));
    }

    public void monthlyAt(String name, Job job, int dayOfMonth, LocalTime time) {
        state.scheduler.addTrigger(new MonthlyTrigger(name, job, dayOfMonth, time));
    }

    private Scheduler createScheduler(ModuleContext context) {
        Scheduler scheduler = new Scheduler(context.logManager);
        if (!context.isTest()) {
            context.startupHook.add(scheduler::start);
            context.shutdownHook.add(scheduler::stop);
            SchedulerController schedulerController = new SchedulerController(scheduler);
            context.httpServer.handler.route.add(HTTPMethod.GET, "/_sys/job", new ControllerHolder(schedulerController::listJobs, true));
            context.httpServer.handler.route.add(HTTPMethod.POST, "/_sys/job/:job", new ControllerHolder(schedulerController::triggerJob, true));
        }
        return scheduler;
    }

    public static class SchedulerConfigState {
        Scheduler scheduler;
    }
}
