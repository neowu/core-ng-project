package core.framework.impl.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public interface DynamicTrigger extends Trigger {
    Duration nextDelay(LocalDateTime now);
}
