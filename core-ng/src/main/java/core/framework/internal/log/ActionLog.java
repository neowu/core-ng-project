package core.framework.internal.log;

import core.framework.log.Markers;
import core.framework.util.Strings;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.framework.internal.log.LogLevel.DEBUG;
import static core.framework.internal.log.LogLevel.WARN;

/**
 * @author neo
 */
public final class ActionLog {
    public static final int MAX_CONTEXT_VALUE_LENGTH = 1000;
    static final int MAX_CONTEXT_VALUES_SIZE = 5000;    // e.g. roughly 5000 "order_id=UUID"=>(8+36+3)*5000=235k

    private static final String LOGGER = LoggerImpl.abbreviateLoggerName(ActionLog.class.getCanonicalName());
    private static final int SOFT_EVENTS_LIMIT = 3000;    // normally 3000 lines trace is about 350k, and limit memory usage for each action

    public final String id;
    public final Instant date;
    public final Map<String, List<String>> context;
    public final Map<String, Double> stats;
    public final WarningContext warningContext;

    final Map<String, PerformanceStat> performanceStats;
    private final List<LogEvent> events;
    private final long startTime;
    public LogLevel result = LogLevel.INFO;
    public Trace trace = Trace.NONE;        // whether flush trace log for all subsequent actions
    public String action = "unassigned";
    public List<String> correlationIds;     // with bulk message handler, there will be multiple correlationIds handled by one batch
    public List<String> clients;
    public List<String> refIds;
    public String errorMessage;
    long elapsed;
    private String errorCode;

    public ActionLog(String message, String id) {
        startTime = System.nanoTime();
        date = Instant.now();
        if (id == null) {
            this.id = LogManager.ID_GENERATOR.next(date);
        } else {
            this.id = id;   // in executor, id is generated in advance to link parent and task
        }
        events = new ArrayList<>(32);   // according to benchmark, ArrayList is as fast as LinkedList with max 3000 items, and has smaller memory footprint
        context = new HashMap<>();  // default capacity is 16, no need to keep insertion order, kibana will sort all keys on display
        stats = new HashMap<>();
        performanceStats = new HashMap<>();
        warningContext = new WarningContext();

        add(event(message));
        add(event("id={}", this.id));
        add(event("date={}", DateTimeFormatter.ISO_INSTANT.format(date)));
        add(event("thread={}", Thread.currentThread().getName()));
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
        for (PerformanceStat stat : performanceStats.values()) {
            stat.checkTotalIO();
        }

        elapsed = elapsed();
        add(event("elapsed={}", elapsed));
        warningContext.checkMaxProcessTime(elapsed);

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
        if (result == LogLevel.INFO) return "OK";
        return result.name();
    }

    boolean flushTraceLog() {
        return trace != Trace.NONE || result.value >= WARN.value;
    }

    public String errorCode() {
        if (errorCode != null) return errorCode;
        if (result.value >= WARN.value) return "UNASSIGNED";
        return null;
    }

    public void context(String key, Object... values) {
        List<String> contextValues = context.computeIfAbsent(key, k -> new ArrayList<>(Math.max(2, values.length)));    // at least use 2 as init capacity, 0 capacity will result in size 10 array after adding
        for (Object value : values) {
            String contextValue = String.valueOf(value);
            if (contextValue.length() > MAX_CONTEXT_VALUE_LENGTH) { // prevent application code from putting large blob as context, e.g. xml or json response
                // use new Error() to print calling stack
                process(new LogEvent(LOGGER, Markers.errorCode("CONTEXT_TOO_LARGE"), WARN, "context value is too long, key={}, value={}", new Object[]{key, contextValue}, new Error("context value is too long")));
            } else if (contextValues.size() >= MAX_CONTEXT_VALUES_SIZE) {
                // try to warn once only, generally if hits here, the app likely will add much more within loop
                if (!"CONTEXT_TOO_LARGE".equals(errorCode))
                    process(new LogEvent(LOGGER, Markers.errorCode("CONTEXT_TOO_LARGE"), WARN, "too many context values, key={}, size={}", new Object[]{key, contextValues.size()}, new Error("too many context values")));
            } else {
                contextValues.add(contextValue);
            }
        }
        add(event("[context] {}={}", key, values.length == 1 ? values[0] : values));
    }

    public void stat(String key, double value) {
        stats.compute(key, (k, oldValue) -> (oldValue == null) ? value : oldValue + value);
        var format = new DecimalFormat();
        add(event("[stat] {}={}", key, format.format(value)));
    }

    public void initializeWarnings(PerformanceWarning[] warnings) {
        for (PerformanceWarning warning : warnings) {
            performanceStats.put(warning.operation, new PerformanceStat(warning));
        }
    }

    public int track(String operation, long elapsed, int readEntries, int writeEntries) {
        PerformanceStat stat = performanceStats.computeIfAbsent(operation, key -> new PerformanceStat(WarningContext.defaultWarning(key)));
        stat.track(elapsed, readEntries, writeEntries);
        return stat.count;
    }

    public String correlationId() {
        if (correlationIds != null && correlationIds.size() == 1) return correlationIds.get(0);
        return id; // if there are multiple correlationIds (in batch), use current id as following correlationId
    }

    public List<String> correlationIds() {
        if (correlationIds != null) return correlationIds;
        return List.of(id);     // current action is root action, use self id as correlationId
    }

    public void action(String action) {
        add(event("action={}", action));
        this.action = action;
    }

    public long remainingProcessTimeInNano() {
        long remainingTime = warningContext.maxProcessTimeInNano - elapsed();
        if (remainingTime < 0) return 0;
        return remainingTime;
    }

    public String trace() {
        var builder = new StringBuilder(events.size() << 7);  // length * 128 as rough initial capacity
        for (LogEvent event : events) {
            event.appendTrace(builder, startTime);
        }
        return builder.toString();
    }

    @Nullable
    public PerformanceWarning[] warnings() {
        List<PerformanceWarning> configs = new ArrayList<>(performanceStats.size());
        for (PerformanceStat stat : performanceStats.values()) {
            if (stat.warning != null) configs.add(stat.warning);
        }
        if (configs.isEmpty()) return null;
        return configs.toArray(new PerformanceWarning[0]);
    }
}
