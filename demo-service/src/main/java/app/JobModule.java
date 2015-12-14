package app;

import app.job.DemoJob;
import core.framework.api.Module;

/**
 * @author neo
 */
public class JobModule extends Module {
    @Override
    protected void initialize() {
        DemoJob job = bind(DemoJob.class);
//        schedule().dailyAt("demo-job", job, LocalTime.of(15, 0));
//        schedule().fixedRate("fixed-rate-job", job, Duration.ofSeconds(10));
    }
}
