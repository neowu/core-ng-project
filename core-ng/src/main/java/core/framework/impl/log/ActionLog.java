package core.framework.impl.log;

import core.framework.api.util.Maps;

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
    private static final int MAX_TRACE_HOLD_SIZE = 3000;    // normal trace 3000 lines is about 350k
    public final String id = UUID.randomUUID().toString();
    final Instant startTime = Instant.now();
    final Map<String, String> context = Maps.newLinkedHashMap();
    final Map<String, PerformanceStat> performanceStats = Maps.newHashMap();
    final List<LogEvent> events = new LinkedList<>();
    private final String logger = LoggerImpl.abbreviateLoggerName(ActionLog.class.getCanonicalName());
    public boolean trace;  // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    String refId;
    String errorMessage;
    long elapsed;
    private LogLevel result = LogLevel.INFO;
    private String errorCode;

    ActionLog(String message) {
        log(message);
        log("[context] id={}", id);
    }

    void end(String message) {
        elapsed = Duration.between(startTime, Instant.now()).toMillis();
        log("[context] elapsed={}", elapsed);
        log(message);
    }

    void process(LogEvent event) {
        if (event.level.value > result.value) {
            result = event.level;
            errorCode = event.errorCode(); // only update error type/message if level raised, so error type will be first WARN or first ERROR
            errorMessage = errorMessage(event);
        }

        int size = events.size();
        if (size < MAX_TRACE_HOLD_SIZE || event.level.value >= LogLevel.WARN.value) {  // after reach max holding lines, only add warning/error events
            events.add(event);
            if (size == MAX_TRACE_HOLD_SIZE)
                log("reached max trace log holding size, only collect critical log event from now on");
        }
    }

    private void log(String message, Object... argument) {  // add log event directly, so internal message and won't be suspended
        LogEvent event = new LogEvent(logger, null, LogLevel.DEBUG, message, argument, null);
        events.add(event);
    }

    private String errorMessage(LogEvent event) {
        String message = event.message();
        if (message.length() > 200)
            return message.substring(0, 200);    // limit 200 chars in action log
        return message;
    }

    String result() {
        if (result == LogLevel.INFO)
            return trace ? "TRACE" : "OK";
        return result.name();
    }

    boolean flushTraceLog() {
        return trace || result.value >= LogLevel.WARN.value;
    }

    String errorCode() {
        if (errorCode != null) return errorCode;
        if (result.value >= LogLevel.WARN.value) return "UNASSIGNED";
        return null;
    }

    public Optional<String> context(String key) {
        return Optional.ofNullable(context.get(key));
    }

    public void context(String key, Object value) {
        log("[context] {}={}", key, value);
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
            log("[context] refId={}", refId);
            this.refId = refId;
        }
    }

    public void action(String action) {
        log("[context] action={}", action);
        this.action = action;
    }
}
