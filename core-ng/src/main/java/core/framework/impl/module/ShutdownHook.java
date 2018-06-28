package core.framework.impl.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author neo
 */
public final class ShutdownHook implements Runnable {
    public static final int STAGE_0 = 0;    // send shutdown signal, begin to shutdown processor for external requests, e.g. http server / kafka listener / scheduler
    public static final int STAGE_1 = 1;    // await external request processor to stop
    public static final int STAGE_2 = 2;    // after no more new external requests, shutdown internal executors / background tasks
    public static final int STAGE_3 = 3;    // after no any task running, shutdown important resources, e.g. kafka producer, log forwarder
    public static final int STAGE_10 = 10;  // release all resources without dependencies
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(25); // default kube terminationGracePeriodSeconds is 30s, here give 25s try to stop important processes
    final Thread thread = new Thread(this, "shutdown");
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private final Map<Integer, List<Shutdown>> stages = new TreeMap<>();

    ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public void add(int stage, Shutdown shutdown) {
        stages.computeIfAbsent(stage, key -> new ArrayList<>()).add(shutdown);
    }

    @Override
    public void run() {
        long endTime = System.currentTimeMillis() + SHUTDOWN_TIMEOUT.toMillis();
        for (Map.Entry<Integer, List<Shutdown>> entry : stages.entrySet()) {
            Integer stage = entry.getKey();
            List<Shutdown> shutdowns = entry.getValue();
            logger.info("shutdown stage: {}", stage);
            for (Shutdown shutdown : shutdowns) {
                try {
                    shutdown.execute(endTime - System.currentTimeMillis());
                } catch (Throwable e) {
                    logger.warn("failed to shutdown, method={}", shutdown, e);
                }
            }
        }
        logger.info("shutdown completed");
    }

    public interface Shutdown {
        void execute(long timeoutInMs) throws Exception;
    }
}
