package core.framework.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class Threads {
    private static final Logger LOGGER = LoggerFactory.getLogger(Threads.class);

    public static void sleepRoughly(Duration duration) {
        long milliseconds = duration.toMillis();
        double times = Randoms.number(0.8, 1.2); // +/-20% random
        long sleepTime = (long) (milliseconds * times);
        LOGGER.debug("sleep {} ms", sleepTime);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("sleep is interrupted", e);
        }
    }
}
