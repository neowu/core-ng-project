package core.framework.impl.log;

import core.framework.impl.log.message.PerformanceStat;
import core.framework.util.Strings;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.framework.impl.log.LogLevel.DEBUG;
import static core.framework.impl.log.LogLevel.WARN;
import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class ActionLog {
    private static final String LOGGER = LoggerImpl.abbreviateLoggerName(ActionLog.class.getCanonicalName());
    private static final ThreadMXBean THREAD = ManagementFactory.getThreadMXBean();

    private static final int MAX_TRACE_HOLD_SIZE = 3000;    // normally 3000 lines trace is about 350k
    private static final int MAX_ERROR_MESSAGE_LENGTH = 200;
    private static final int MAX_CONTEXT_VALUE_LENGTH = 1000;

    public final String id;
    final Instant date;
    final Map<String, String> context;
    final Map<String, PerformanceStat> performanceStats;
    final List<LogEvent> events;
    final long startTime;
    private final long startCPUTime;

    public boolean trace;  // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    public List<String> correlationIds;    // with bulk message handler, there will be multiple correlationIds handled by one batch
    public List<String> clients;
    public List<String> refIds;
    Map<String, Double> stats;

    String errorMessage;
    long elapsed;
    long cpuTime;

    private LogLevel result = LogLevel.INFO;
    private String errorCode;

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
            errorMessage = Strings.truncate(event.message(), MAX_ERROR_MESSAGE_LENGTH);     // limit error message length in action log
        }
        if (event.level.value >= WARN.value || events.size() < MAX_TRACE_HOLD_SIZE) {       // after reach max holding lines, only add warning/error events
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
        if (events.size() == MAX_TRACE_HOLD_SIZE) {
            events.add(event("reached max trace log holding size, only collect critical log event from now on"));
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

    public Optional<String> context(String key) {
        return Optional.ofNullable(context.get(key));
    }

    public void context(String key, Object value) {
        String contextValue = String.valueOf(value);
        if (contextValue.length() > MAX_CONTEXT_VALUE_LENGTH) { // prevent application code from putting large blob as context, e.g. xml or json response
            throw new Error(format("context value is too long, key={}, value={}...(truncated)", key, contextValue.substring(0, MAX_CONTEXT_VALUE_LENGTH)));
        }
        String previous = context.put(key, contextValue);
        // put context can be called by application code, check duplication to avoid producing huge trace log by accident
        if (previous != null) throw new Error(format("found duplicate context key, key={}, value={}, previous={}", key, contextValue, previous));
        add(event("[context] {}={}", key, contextValue));
    }

    public void stat(String key, double value) {
        if (stats == null) stats = new HashMap<>();
        stats.compute(key, (k, oldValue) -> (oldValue == null) ? value : oldValue + value);
        add(event("[stat] {}={}", key, value));
    }

    public void track(String action, long elapsed, Integer readEntries, Integer writeEntries) {
        PerformanceStat stat = performanceStats.computeIfAbsent(action, key -> createPerformanceStat());
        stat.count += 1;
        stat.totalElapsed += elapsed;
        if (readEntries != null) {
            if (stat.readEntries == null) stat.readEntries = readEntries;
            else stat.readEntries += readEntries;
        }
        if (writeEntries != null) {
            if (stat.writeEntries == null) stat.writeEntries = writeEntries;
            else stat.writeEntries += writeEntries;
        }
        // not to add event to keep trace log concise
    }

    private PerformanceStat createPerformanceStat() {
        var stat = new PerformanceStat();
        stat.count = 0;
        stat.totalElapsed = 0L;
        return stat;
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
