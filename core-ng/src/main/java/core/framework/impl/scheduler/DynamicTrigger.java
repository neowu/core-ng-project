package core.framework.impl.scheduler;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
public interface DynamicTrigger extends Trigger {
    ZonedDateTime next(ZonedDateTime now);
}
