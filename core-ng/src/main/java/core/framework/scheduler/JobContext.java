package core.framework.scheduler;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class JobContext {
    public final String name;
    public final ZonedDateTime scheduledTime;

    public JobContext(String name, ZonedDateTime scheduledTime) {
        this.name = name;
        this.scheduledTime = scheduledTime;
    }
}
