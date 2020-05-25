package core.framework.internal.log;

import core.framework.util.Strings;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static core.framework.internal.log.LogLevel.DEBUG;
import static core.framework.internal.log.LogLevel.WARN;
import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class ActionLog {
    static final int MAX_CONTEXT_VALUE_LENGTH = 1000;

    private static final String LOGGER = LoggerImpl.abbreviateLoggerName(ActionLog.class.getCanonicalName());
    private static final ThreadMXBean THREAD = ManagementFactory.getThreadMXBean();
    private static final int SOFT_EVENTS_LIMIT = 3000;    // normally 3000 lines trace is about 350k

    public final String id;
    public final Map<String, List<String>> context;
    public final Instant date;
    final Map<String, PerformanceStat> performanceStats;
    final List<LogEvent> events;
    final long startTime;
    private final long startCPUTime;

    public boolean trace;  // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    public List<String> correlationIds;    // with bulk message handler, there will be multiple correlationIds handled by one batch
    public List<String> clients;
    public List<String> refIds;
    public Map<String, Double> stats;

    String errorMessage;
    long elapsed;
    long cpuTime;

    private LogLevel result = LogLevel.INFO;
    private String errorCode;

    public boolean enableSlowSQLWarning = true;

    public ActionLog(String message) {
        startTime = System.nanoTime();
        startCPUTime = THREAD.getCurrentThreadCpuTime();
        date = Instant.now();
        id = LogManager.ID_GENERATOR.next(date);
        events = new ArrayList<>(32);   // according to benchmark, ArrayList is as fast as LinkedList with max 3000 items, and has smaller memory footprint
        context = new LinkedHashMap<>();
        performanceStats = new HashMap<>();

        add(event(message));
        add(event("id={}", id));
        add(event("date={}", DateTimeFormatter.ISO_INSTANT.format(date)));
        Thread thread = Thread.currentThread();
        add(event("thread={}", thread.getName()));
    }

    void process(LogEvent event) {
        if (event.level.value > result.value) {
            result = event.level;
            errorCode = event.errorCode();      // only update errorCode/message if level raised, so errorCode will be first WARN or ERROR
            errorMessage = Strings.truncate(event.message(), MAX_CONTEXT_VALUE_LENGTH);     // limit error message length in action log
        }
        if (event.level.value >= WARN.value || events.size() < SOFT_EVENTS_LIMIT) {       // after reach max holding lines, only add warning/error events
            add(event);
        }
    }

    void end(String message) {
        cpuTime = THREAD.getCurrentThreadCpuTime() - startCPUTime;
        elapsed = elapsed();
        add(event("elapsed={}", elapsed));
        add(event(message));
    }

    public long elapsed() {
        return System.nanoTime() - startTime;
    }

    private void add(LogEvent event) {  // log inside action log will call this to add log event directly, so internal message won't be suspended
        events.add(event);
        if (events.size() == SOFT_EVENTS_LIMIT) {
            events.add(event("...(soft trace limit reached)"));
        }
    }

    private LogEvent event(String message, Object... arguments) {
        return new LogEvent(LOGGER, null, DEBUG, message, arguments, null);
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

    public void context(String key, Object... values) {
        List<String> contextValues = context.computeIfAbsent(key, k -> new ArrayList<>(Math.max(2, values.length)));    // at least use 2 as init capacity, 0 capacity will result in size 10 array after adding
        for (Object value : values) {
            String contextValue = String.valueOf(value);
            if (contextValue.length() > MAX_CONTEXT_VALUE_LENGTH) { // prevent application code from putting large blob as context, e.g. xml or json response
                throw new Error(format("context value is too long, key={}, value={}...(truncated)", key, contextValue.substring(0, MAX_CONTEXT_VALUE_LENGTH)));
            }
            contextValues.add(contextValue);
            add(event("[context] {}={}", key, contextValue));
        }
    }

    public void stat(String key, double value) {
        if (stats == null) stats = new HashMap<>();
        stats.compute(key, (k, oldValue) -> (oldValue == null) ? value : oldValue + value);
        var format = new DecimalFormat();
        add(event("[stat] {}={}", key, format.format(value)));
    }

    public int track(String operation, long elapsed, int readEntries, int writeEntries) {
        PerformanceStat stat = performanceStats.computeIfAbsent(operation, key -> new PerformanceStat());
        stat.count += 1;
        stat.totalElapsed += elapsed;
        stat.readEntries += readEntries;
        stat.writeEntries += writeEntries;
        // not to add event to keep trace log concise
        return stat.count;
    }

    public String correlationId() {
        if (correlationIds != null && correlationIds.size() == 1) return correlationIds.get(0);
        return id; // if there are multiple correlationIds (in batch), use current id as following correlationId
    }

    public void action(String action) {
        add(event("action={}", action));
        this.action = action;
    }
}
