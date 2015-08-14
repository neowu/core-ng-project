package core.framework.impl.log;

import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public class ActionLog {
    private final Logger logger = LoggerFactory.getLogger(ActionLog.class);

    final Instant startTime = Instant.now();
    public final String id = UUID.randomUUID().toString();
    private LogLevel result = LogLevel.INFO;
    public boolean trace;  // whether always write trace log for all subsequent actions
    public String action = "unassigned";
    String refId;
    String errorMessage;
    Class<?> exceptionClass;
    long elapsed;
    final Map<String, String> context = Maps.newLinkedHashMap();
    final Map<String, PerformanceStat> performanceStats = Maps.newHashMap();

    void end() {
        elapsed = Duration.between(startTime, Instant.now()).toMillis();
    }

    void updateResult(LogLevel level) {
        if (level.value > result.value) result = level;
    }

    public String result() {
        return result == LogLevel.INFO ? "OK" : String.valueOf(result);
    }

    public Optional<String> context(String key) {
        return Optional.ofNullable(context.get(key));
    }

    public void context(String key, Object value) {
        logger.debug("[context] {}={}", key, value);
        context.put(key, String.valueOf(value));
    }

    public void track(String action, long elapsedTime) {
        PerformanceStat tracking = performanceStats.computeIfAbsent(action, key -> new PerformanceStat());
        tracking.count++;
        tracking.totalElapsed += elapsedTime;
    }

    public String refId() {
        if (refId == null) return id;
        return refId;
    }

    public void refId(String refId) {
        if (refId != null) {
            logger.debug("[context] refId={}", refId);
            this.refId = refId;
        }
    }

    public void action(String action) {
        logger.debug("[context] action={}", action);
        this.action = action;
    }

    public void triggerTraceLog() {
        logger.warn("trigger trace log, id={}, action={}", id, action);
        trace = true;
    }

    void error(Throwable e) {
        errorMessage = e.getMessage();
        exceptionClass = e.getClass();
    }
}
