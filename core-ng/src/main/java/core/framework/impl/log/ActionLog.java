package core.framework.impl.log;

import core.framework.api.log.Warning;
import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public class ActionLog {
    private final Logger logger = LoggerFactory.getLogger(ActionLog.class);

    final Instant startTime = Instant.now();
    private LogLevel result = LogLevel.INFO;
    String id = UUID.randomUUID().toString();
    private String action = "unassigned";
    String refId;
    public boolean trace;  // whether always write trace log for all subsequent actions
    String errorMessage;
    Class<?> exceptionClass;
    long elapsed;
    final Map<String, String> context = new LinkedHashMap<>();
    final Map<String, PerformanceStat> performanceStats = Maps.newHashMap();

    public ActionLog() {
        logger.debug("[context] id={}", id);
    }

    void updateResult(LogLevel level) {
        if (level.value > result.value) result = level;
    }

    String result() {
        return result == LogLevel.INFO ? "OK" : String.valueOf(result);
    }

    void end() {
        elapsed = Duration.between(startTime, Instant.now()).toMillis();
    }

    public Optional<String> getContext(String key) {
        return Optional.ofNullable(context.get(key));
    }

    public void putContext(String key, Object value) {
        logger.debug("[context] {}={}", key, value);
        context.put(key, String.valueOf(value));
    }

    public void track(String action, long elapsedTime) {
        PerformanceStat tracking = this.performanceStats.computeIfAbsent(action, key -> new PerformanceStat());
        tracking.count++;
        tracking.totalElapsedTime += elapsedTime;
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

    public String action() {
        return action;
    }

    public void action(String action) {
        logger.debug("[context] action={}", action);
        this.action = action;
    }

    public void triggerTraceLog() {
        logger.warn("trigger trace log, id={}, action={}", id, action);
        trace = true;
    }

    public void error(Throwable e) {
        errorMessage = e.getMessage();
        exceptionClass = e.getClass();

        if (e.getClass().isAnnotationPresent(Warning.class)) {
            logger.warn(errorMessage, e);
        } else {
            logger.error(errorMessage, e);
        }
    }
}
