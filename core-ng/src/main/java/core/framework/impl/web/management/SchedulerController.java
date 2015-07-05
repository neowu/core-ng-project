package core.framework.impl.web.management;

import core.framework.api.http.ContentTypes;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.impl.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SchedulerController {
    private final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    private final Scheduler scheduler;

    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Response triggerJob(Request request) {
        ControllerHelper.validateFromLocalNetwork(request.clientIP());

        String jobName = request.pathParam("job");
        logger.info("trigger job, jobName={}, clientIP={}", jobName, request.clientIP());
        scheduler.triggerNow(jobName);
        return Response.text("job triggered, name=" + jobName, ContentTypes.TEXT_PLAIN);
    }
}
