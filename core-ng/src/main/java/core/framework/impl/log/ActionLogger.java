package core.framework.impl.log;

import core.framework.api.util.Lists;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author neo
 */
class ActionLogger {
    private static final int MAX_HOLD_SIZE = 5000;

    private final ActionLogWriter actionLogWriter;
    private final TraceLogWriter traceLogWriter;
    private final LogForwarder logForwarder;

    final ActionLog log = new ActionLog();

    private List<LogEvent> events = new LinkedList<>();
    private int size = 0;
    private Writer traceWriter;

    public ActionLogger(ActionLogWriter actionLogWriter, TraceLogWriter traceLogWriter, LogForwarder logForwarder) {
        this.actionLogWriter = actionLogWriter;
        this.traceLogWriter = traceLogWriter;
        this.logForwarder = logForwarder;
    }

    public void process(LogEvent event) {
        log.updateResult(event.level);
        size++;
        if (events != null) {
            events.add(event);
            if (event.level.value >= LogLevel.WARN.value || size >= MAX_HOLD_SIZE) {
                flushTraceLogs();
                events = null;
            }
        } else {
            writeTraceLog(event);
        }
    }

    public void end() {
        log.end();

        if (actionLogWriter != null) actionLogWriter.write(log);

        if (traceWriter != null) traceLogWriter.closeWriter(traceWriter);

        if (logForwarder != null) logForwarder.forwardActionLog(log);
    }

    private void flushTraceLogs() {
        if (traceLogWriter != null) {
            traceWriter = traceLogWriter.createWriter(log);

            for (LogEvent event : events) {
                traceLogWriter.write(traceWriter, event);
            }
        }

        if (logForwarder != null) {
            logForwarder.forwardTraceLog(log, events);
        }
    }

    void writeTraceLog(LogEvent event) {
        if (size == MAX_HOLD_SIZE + 1) {
            log.updateResult(LogLevel.WARN);
            LogEvent warning = new LogEvent(LogLevel.WARN, System.currentTimeMillis(), LoggerImpl.abbreviateLoggerName(ActionLogger.class.getCanonicalName()), "reached max holding size of trace log, please contact arch team to split big task into smaller batch", null, null);

            if (traceLogWriter != null) traceLogWriter.write(traceWriter, warning);

            if (logForwarder != null) logForwarder.forwardTraceLog(log, Lists.newArrayList(warning));
        }

        if (traceLogWriter != null) traceLogWriter.write(traceWriter, event);

        if (logForwarder != null && size <= MAX_HOLD_SIZE) {    // not forward trace to queue if more than max lines.
            logForwarder.forwardTraceLog(log, Lists.newArrayList(event));
        }
    }
}
