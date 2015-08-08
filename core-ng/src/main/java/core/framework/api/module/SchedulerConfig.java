package core.framework.api.module;

import core.framework.api.scheduler.Job;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.scheduler.trigger.DailyTrigger;
import core.framework.impl.scheduler.trigger.FixedRateTrigger;
import core.framework.impl.web.management.SchedulerController;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class SchedulerConfig {
    private final ModuleContext context;

    public SchedulerConfig(ModuleContext context) {
        this.context = context;
    }

    private Scheduler scheduler() {
        if (context.scheduler != null) {
            return context.scheduler;
        } else {
            Scheduler scheduler = new Scheduler(context.executor, context.logManager);
            if (!context.test) {
                context.startupHook.add(scheduler::start);
                context.shutdownHook.add(scheduler::shutdown);

                SchedulerController schedulerController = new SchedulerController(scheduler);
                context.httpServer.post("/management/job/:job", schedulerController::triggerJob);
            }
            context.scheduler = scheduler;
            return scheduler;
        }
    }

    public void fixedRate(String name, Job job, Duration rate) {
        scheduler().addTrigger(name, job, new FixedRateTrigger(rate));
    }

    public void dailyAt(String name, Job job, LocalTime time) {
        scheduler().addTrigger(name, job, new DailyTrigger(time));
    }
}
