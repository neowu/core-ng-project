package core.framework.internal.web.sys;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.internal.scheduler.Scheduler;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.web.Request;
import core.framework.web.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class SchedulerController {
    private final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    private final IPv4AccessControl accessControl = new IPv4AccessControl();
    private final Scheduler scheduler;

    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Response jobs(Request request) {
        accessControl.validate(request.clientIP());

        var response = new ListJobResponse();
        List<ListJobResponse.JobView> jobs = new ArrayList<>();
        scheduler.tasks.forEach((name, trigger) -> {
            var job = new ListJobResponse.JobView();
            job.name = trigger.name();
            job.jobClass = trigger.job().getClass().getCanonicalName();
            job.trigger = trigger.trigger();
            jobs.add(job);
        });
        response.jobs = jobs;
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    public Response triggerJob(Request request) {
        accessControl.validate(request.clientIP());

        String job = request.pathParam("job");
        logger.warn(Markers.errorCode("MANUAL_OPERATION"), "trigger job manually, job={}", job);   // log trace message, due to potential impact
        scheduler.triggerNow(job, ActionLogContext.id());
        return Response.text("job triggered, job=" + job).status(HTTPStatus.ACCEPTED);
    }
}
