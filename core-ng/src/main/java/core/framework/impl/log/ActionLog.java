package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.util.Exceptions;
import core.framework.util.Maps;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static core.framework.impl.log.LogLevel.DEBUG;
import static core.framework.impl.log.LogLevel.WARN;

/**
 * @author neo
 */
public final class ActionLog {
    private static final ThreadMXBean THREAD = ManagementFactory.getThreadMXBean();

    private static final int MAX_TRACE_HOLD_SIZE = 3000;    // normal trace 3000 lines is about 350k
    private static final int MAX_ERROR_MESSAGE_LENGTH = 200;
    private static final int MAX_CONTEXT_VALUE_LENGTH = 1000;
    private static final String LOGGER = LoggerImpl.abbreviateLoggerName(ActionLog.class.getCanonicalName());
    public final String id;
    final Instant date;
    final Map<String, String> context;
    final Map<String, Double> stats;
    final Map<String, PerformanceStat> performanceStats;
    final List<LogEvent> events;
    private final LogFilter filter;
    private final long startCPUTime;
    private final long startElapsed;
    public boolean trace;  // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    String refId;
    String errorMessage;
    long elapsed;
    long cpuTime;
    private LogLevel result = LogLevel.INFO;
    private String errorCode;

    public ActionLog(String message, LogFilter filter) {
        startElapsed = System.nanoTime();
        startCPUTime = THREAD.getCurrentThreadCpuTime();
        date = Instant.now();

        this.filter = filter;
        events = new LinkedList<>();
        context = Maps.newLinkedHashMap();
        stats = Maps.newLinkedHashMap();
        performanceStats = Maps.newHashMap();
        id = UUID.randomUUID().toString();

        add(event(message));
        add(event("[context] id={}", id));
    }

    void end(String message) {
        cpuTime = THREAD.getCurrentThreadCpuTime() - startCPUTime;
        elapsed = elapsedTime();
        add(event("[context] elapsed={}", elapsed));
        add(event(message));
    }

    public long elapsedTime() {
        return System.nanoTime() - startElapsed;
    }

    void process(LogEvent event) {
        if (event.level.value > result.value) {
            result = event.level;
            errorCode = event.errorCode(); // only update error type/message if level raised, so error type will be first WARN or first ERROR
            errorMessage = errorMessage(event);
        }
        if (events.size() < MAX_TRACE_HOLD_SIZE || event.level.value >= WARN.value) {  // after reach max holding lines, only add warning/error events
            add(event);
        }
    }

    private void add(LogEvent event) {  // log inside action log will call this to add log event directly, so internal message won't be suspended
        events.add(event);
        if (events.size() == MAX_TRACE_HOLD_SIZE) {
            events.add(event("reached max trace log holding size, only collect critical log event from now on"));
        }
    }

    private LogEvent event(String message, Object... argument) {
        return new LogEvent(LOGGER, null, DEBUG, message, argument, null, filter);
    }

    private String errorMessage(LogEvent event) {
        String message = event.message();
        if (message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH)
            return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);    // limit error message length in action log
        return message;
    }

    String result() {
        if (result == LogLevel.INFO)
            return trace ? "TRACE" : "OK";
        return result.name();
    }

    boolean flushTraceLog() {
        return trace || result.value >= WARN.value;
    }

    String errorCode() {
        if (errorCode != null) return errorCode;
        if (result.value >= WARN.value) return "UNASSIGNED";
        return null;
    }

    public Optional<String> context(String key) {
        return Optional.ofNullable(context.get(key));
    }

    public void context(String key, Object value) {
        String contextValue = String.valueOf(value);
        if (contextValue.length() > MAX_CONTEXT_VALUE_LENGTH) { // prevent application code from putting large blob as context, e.g. xml or json response
            throw Exceptions.error("context value is too long, key={}, value={}...(truncated)", key, contextValue.substring(0, MAX_CONTEXT_VALUE_LENGTH));
        }
        String previous = context.put(key, contextValue);
        // put context can be called by application code, check duplication to avoid producing huge trace log by accident
        if (previous != null) throw Exceptions.error("found duplicate context key, key={}, value={}, previous={}", key, contextValue, previous);
        add(event("[context] {}={}", key, contextValue));
    }

    public void stat(String key, Number value) {
        stats.compute(key, (k, oldValue) -> (oldValue == null) ? value.doubleValue() : oldValue + value.doubleValue());
        add(event("[stat] {}={}", key, value));
    }

    public void track(String action, long elapsedTime, Integer readEntries, Integer writeEntries) {
        PerformanceStat stat = performanceStats.computeIfAbsent(action, key -> new PerformanceStat());
        stat.count++;
        stat.totalElapsed += elapsedTime;
        stat.increaseReadEntries(readEntries);
        stat.increaseWriteEntries(writeEntries);
        add(event("[track] {}, elapsedTime={}, readEntries={}, writeEntries={}", action, elapsedTime, readEntries, writeEntries));
    }

    public String refId() {
        if (refId == null) return id;
        return refId;
    }

    public void refId(String refId) {
        if (refId != null) {
            add(event("[context] refId={}", refId));
            this.refId = refId;
        }
    }

    public void action(String action) {
        add(event("[context] action={}", action));
        this.action = action;
    }
}
