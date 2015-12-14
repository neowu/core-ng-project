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
public final class ActionLog {
    public final String id = UUID.randomUUID().toString();
    final Instant startTime = Instant.now();
    final Map<String, String> context = Maps.newLinkedHashMap();
    final Map<String, PerformanceStat> performanceStats = Maps.newHashMap();
    private final Logger logger = LoggerFactory.getLogger(ActionLog.class);
    public boolean trace;  // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    String refId;
    String errorMessage;
    long elapsed;
    LogLevel result = LogLevel.INFO;
    private String errorType;

    void end() {
        elapsed = Duration.between(startTime, Instant.now()).toMillis();
    }

    void process(LogEvent event) {
        if (event.level.value > result.value) result = event.level;
        String errorType = event.errorType();
        if (errorType != null) {
            this.errorType = errorType;
            this.errorMessage = event.message();
        }
        if (event.trace()) {
            trace = true;
        }
    }

    String result() {
        if (result == LogLevel.INFO) {
            return trace ? "TRACE" : "OK";
        }
        return String.valueOf(result);
    }

    String errorType() {
        if (errorType != null) return errorType;
        if (result.value >= LogLevel.WARN.value) return "UNASSIGNED";
        return null;
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
}
