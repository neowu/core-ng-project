package core.framework.scheduler;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
@FunctionalInterface
public interface Trigger {
    ZonedDateTime next(ZonedDateTime previous);
}
