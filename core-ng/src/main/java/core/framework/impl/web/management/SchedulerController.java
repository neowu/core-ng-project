package core.framework.impl.web.management;

import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.log.Markers;
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
    private final IPAccessControl accessControl = new IPAccessControl();
    private final Scheduler scheduler;

    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Response jobs(Request request) {
        accessControl.validate(request.clientIP());

        List<JobView> jobs = Lists.newArrayList();
        scheduler.tasks.forEach((name, trigger) -> {
            JobView job = new JobView();
            job.name = trigger.name();
            job.jobClass = trigger.job().getClass().getCanonicalName();
            job.trigger = trigger.trigger();
            jobs.add(job);
        });
        return Response.bean(jobs);
    }

    public Response triggerJob(Request request) {
        accessControl.validate(request.clientIP());

        String job = request.pathParam("job");
        logger.warn(Markers.errorCode("MANUAL_OPERATION"), "trigger job manually, job={}", job);   // log trace message, due to potential impact
        scheduler.triggerNow(job);
        return Response.text("job triggered, job=" + job);
    }
}
