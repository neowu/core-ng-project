package core.framework.impl.web.management;

import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.util.Lists;
import core.framework.web.Request;
import core.framework.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public class SchedulerController {
    private final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    private final Scheduler scheduler;
    private final IPAccessControl accessControl;

    public SchedulerController(Scheduler scheduler, IPAccessControl accessControl) {
        this.scheduler = scheduler;
        this.accessControl = accessControl;
    }

    public Response jobs(Request request) {
        accessControl.validateClientIP(request.clientIP());

        List<JobView> jobs = Lists.newArrayList();
        scheduler.triggers.forEach((name, trigger) -> {
            JobView job = new JobView();
            job.name = trigger.name();
            job.jobClass = trigger.job().getClass().getCanonicalName();
            job.frequency = trigger.frequency();
            jobs.add(job);
        });
        return Response.bean(jobs);
    }

    public Response triggerJob(Request request) {
        accessControl.validateClientIP(request.clientIP());

        String job = request.pathParam("job");
        logger.info("trigger job, job={}", job);
        scheduler.triggerNow(job);
        return Response.text("job triggered, job=" + job);
    }
}
