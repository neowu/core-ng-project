package core.framework.internal.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class WarningContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningContext.class);

    public boolean suppressSlowSQLWarning;
    long maxProcessTimeInNano;

    public void maxProcessTimeInNano(long maxProcessTimeInNano) {
        this.maxProcessTimeInNano = maxProcessTimeInNano;
        LOGGER.debug("maxProcessTime={}", maxProcessTimeInNano);
    }

    public void checkMaxProcessTime(long elapsed) {
        if (maxProcessTimeInNano > 0 && elapsed > maxProcessTimeInNano) {
            LOGGER.warn(errorCode("SLOW_PROCESS"), "action took longer than of max process time, maxProcessTime={}, elapsed={}", Duration.ofNanos(maxProcessTimeInNano), Duration.ofNanos(elapsed));
        }
    }
}
