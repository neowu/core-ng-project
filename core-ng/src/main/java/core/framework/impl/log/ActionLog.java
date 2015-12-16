package core.framework.impl.log;

import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public final class ActionLog {
    private static final int MAX_TRACE_HOLD_SIZE = 5000;
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
    List<LogEvent> events = new LinkedList<>();
    private String errorType;

    void process(LogEvent event) {
        if (event.level.value > result.value) {
            result = event.level;
        }
        String errorType = event.errorType();
        if (errorType != null) {
            this.errorType = errorType;
            this.errorMessage = event.message();
        }
        if (events.size() < MAX_TRACE_HOLD_SIZE) {
            events.add(event);
        }
    }

    void end() {
        if (events.size() == MAX_TRACE_HOLD_SIZE) {
            String message = "reached max holding size of trace log, please contact arch team";
            if (result.value < LogLevel.WARN.value) result = LogLevel.WARN;
            this.errorType = "TRACE_LOG_TOO_LONG";
            this.errorMessage = message;
            LogEvent warning = new LogEvent(logger.getName(), null, LogLevel.WARN, message, null, null);
            events.add(warning);
        }
        elapsed = Duration.between(startTime, Instant.now()).toMillis();
    }

    String result() {
        if (result == LogLevel.INFO) {
            return trace ? "TRACE" : "OK";
        }
        return String.valueOf(result);
    }

    boolean flushTraceLog() {
        return trace || result.value >= LogLevel.WARN.value;
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
