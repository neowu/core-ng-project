package core.framework.internal.module;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.util.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class ShutdownHook implements Runnable {
    public static final int STAGE_0 = 0;    // send shutdown signal, begin to shutdown processor for external requests, e.g. http server / kafka listener / scheduler
    public static final int STAGE_1 = 1;    // await external request processor to stop
    public static final int STAGE_2 = 2;    // after no more new external requests, shutdown internal executors / background tasks
    public static final int STAGE_3 = 3;    // await internal executors to stop
    public static final int STAGE_4 = 4;    // after no any task running, shutdown kafka producer
    public static final int STAGE_5 = 5;    // release all application defined shutdown hook
    public static final int STAGE_7 = 7;    // release all resources without dependencies, e.g. db / redis / mongo / search
    public static final int STAGE_8 = 8;    // shutdown kafka log appender, give more time try to forward all logs
    public static final int STAGE_9 = 9;    // finally stop the http server, to make sure it responses to incoming requests during shutdown

    final Thread thread = new Thread(this, "shutdown");
    private final long shutdownTimeoutInMs;
    private final LogManager logManager;
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private final ShutdownStage[] stages = new ShutdownStage[STAGE_9 + 1];

    ShutdownHook(LogManager logManager) {
        shutdownTimeoutInMs = shutdownTimeoutInMs(System.getenv());
        this.logManager = logManager;
        Runtime.getRuntime().addShutdownHook(thread);
    }

    long shutdownTimeoutInMs(Map<String, String> env) {
        String shutdownTimeout = env.get("SHUTDOWN_TIMEOUT_IN_SEC");
        if (shutdownTimeout != null) {
            Duration timeout = Duration.ofSeconds(Long.parseLong(shutdownTimeout));
            if (timeout.isZero() || timeout.isNegative()) throw new Error("shutdown timeout must be greater than 0, timeout=" + shutdownTimeout);
            return timeout.toMillis();
        }
        return Duration.ofSeconds(25).toMillis(); // default kube terminationGracePeriodSeconds is 30s, here give 25s try to stop important processes
    }

    public void add(int stage, Shutdown shutdown) {
        if (stages[stage] == null) stages[stage] = new ShutdownStage();
        stages[stage].shutdowns.add(shutdown);
    }

    @Override
    public void run() {
        ActionLog actionLog = logManager.begin("=== shutdown begin ===");
        logContext(actionLog);

        long endTime = System.currentTimeMillis() + shutdownTimeoutInMs;

        shutdown(endTime, STAGE_0, STAGE_5);
        logManager.end("=== shutdown end ==="); // end action log before closing es/log appender

        shutdown(endTime, STAGE_7, STAGE_9);
        logger.info("shutdown completed, elapsed={}", actionLog.elapsed());
    }

    void logContext(ActionLog actionLog) {
        actionLog.action("app:stop");
        actionLog.context("host", Network.LOCAL_HOST_NAME);
        actionLog.context("start_time", Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()));
        actionLog.stat("uptime_in_ms", ManagementFactory.getRuntimeMXBean().getUptime());
    }

    private void shutdown(long endTime, int fromStage, int toStage) {
        for (int i = fromStage; i <= toStage; i++) {
            ShutdownStage stage = stages[i];
            if (stage == null) continue;
            logger.info("shutdown stage: {}", i);
            for (Shutdown shutdown : stage.shutdowns) {
                try {
                    shutdown.execute(endTime - System.currentTimeMillis());
                } catch (Throwable e) {
                    logger.warn(errorCode("FAILED_TO_STOP"), "failed to shutdown, method={}", shutdown, e);
                }
            }
        }
    }

    public interface Shutdown {
        void execute(long timeoutInMs) throws Exception;
    }

    static class ShutdownStage {
        final List<Shutdown> shutdowns = new ArrayList<>();
    }
}
