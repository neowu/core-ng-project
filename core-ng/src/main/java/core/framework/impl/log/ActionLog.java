package core.framework.impl.log;

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
    public final Instant startTime = Instant.now();
    private LogLevel result = LogLevel.INFO;
    public String id = UUID.randomUUID().toString();
    public String action = "unassigned";
    String refId;
    public boolean trace;  // whether always write trace log for all subsequent actions
    public String errorMessage;
    public Class<?> exceptionClass;
    long elapsed;
    final Map<String, String> context = new LinkedHashMap<>();
    final Map<String, TimeTracking> tracking = Maps.newHashMap();

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
        TimeTracking tracking = this.tracking.computeIfAbsent(action, key -> new TimeTracking());
        tracking.count++;
        tracking.totalElapsedTime += elapsedTime;
    }

    public String refId() {
        if (refId == null) return id;
        return refId;
    }

    public void refId(String refId) {
        this.refId = refId;
    }
}
