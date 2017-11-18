package core.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public final class Threads {
    private static final Logger LOGGER = LoggerFactory.getLogger(Threads.class);

    public static void sleepRoughly(Duration duration) {
        long sleepTime = sleepTime(duration);
        LOGGER.debug("sleep {} ms", sleepTime);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.debug("sleep is interrupted", e);
        }
    }

    // due to in docker/kubernetes environment, Runtime.getRuntime().availableProcessors() always return cores of host, not reflecting limits.cpu or cpus params,
    // wit this way, it can pass -Dcore.availableProcessors to override the value
    public static int availableProcessors() {
        String availableProcessors = System.getProperty("core.availableProcessors");
        if (availableProcessors != null) {
            return Integer.parseInt(availableProcessors);
        }
        return Runtime.getRuntime().availableProcessors();
    }

    static long sleepTime(Duration duration) {
        long milliseconds = duration.toMillis();
        double times = Randoms.number(0.8, 1.2); // +/-20% random
        return (long) (milliseconds * times);
    }
}
