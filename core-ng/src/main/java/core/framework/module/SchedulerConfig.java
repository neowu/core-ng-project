package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.inject.InjectValidator;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.scheduler.DailyTrigger;
import core.framework.internal.scheduler.HourlyTrigger;
import core.framework.internal.scheduler.MonthlyTrigger;
import core.framework.internal.scheduler.Scheduler;
import core.framework.internal.scheduler.WeeklyTrigger;
import core.framework.internal.web.sys.SchedulerController;
import core.framework.scheduler.Job;
import core.framework.scheduler.Trigger;
import core.framework.util.Strings;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public final class SchedulerConfig extends Config {
    private Scheduler scheduler;
    private boolean triggerAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        var scheduler = new Scheduler(context.logManager);
        context.startupHook.start.add(scheduler::start);
        context.shutdownHook.add(ShutdownHook.STAGE_0, timeout -> scheduler.shutdown());
        context.shutdownHook.add(ShutdownHook.STAGE_1, scheduler::awaitTermination);

        var schedulerController = new SchedulerController(scheduler);
        context.route(HTTPMethod.GET, "/_sys/job", (LambdaController) schedulerController::jobs, true);
        context.route(HTTPMethod.POST, "/_sys/job/:job", (LambdaController) schedulerController::triggerJob, true);

        this.scheduler = scheduler; // make lambda not refer to this class/field
    }

    public void timeZone(ZoneId zoneId) {
        if (triggerAdded) throw new Error("schedule().timeZone() must be configured before adding trigger");
        if (zoneId == null) throw new Error("zoneId must not be null");
        scheduler.clock = Clock.system(zoneId);
    }

    public void fixedRate(String name, Job job, Duration rate) {
        validateJob(name, job);

        scheduler.addFixedRateTask(name, job, rate);
        triggerAdded = true;
    }

    public void hourlyAt(String name, Job job, int minute, int second) {
        trigger(name, job, new HourlyTrigger(minute, second));
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
        validateJob(name, job);

        scheduler.addTriggerTask(name, job, trigger);
        triggerAdded = true;
    }

    void validateJob(String name, Job job) {
        Class<? extends Job> jobClass = job.getClass();
        if (jobClass.isSynthetic())
            throw new Error(Strings.format("job class must not be anonymous class or lambda, please create static class, name={}, jobClass={}", name, jobClass.getCanonicalName()));
        new InjectValidator(job).validate();
    }
}
