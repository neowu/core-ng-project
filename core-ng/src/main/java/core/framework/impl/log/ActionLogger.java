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

    private final LogWriter logWriter;
    private final LogForwarder logForwarder;

    final ActionLog log = new ActionLog();
    private List<LogEvent> events = new LinkedList<>();
    private int size = 0;
    private Writer traceWriter;

    public ActionLogger(LogWriter logWriter, LogForwarder logForwarder) {
        this.logWriter = logWriter;
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

        logWriter.writeActionLog(log);
        if (traceWriter != null) logWriter.closeTraceLogWriter(traceWriter);

        if (logForwarder != null) logForwarder.queueActionLog(log);
    }

    private void flushTraceLogs() {
        traceWriter = logWriter.createTraceWriter(log);

        for (LogEvent event : events) {
            logWriter.writeTraceLog(traceWriter, event);
        }

        if (logForwarder != null) {
            logForwarder.queueTraceLog(log, events);
        }
    }

    void writeTraceLog(LogEvent event) {
        if (size == MAX_HOLD_SIZE + 1) {
            log.updateResult(LogLevel.WARN);

            LogEvent warning = new LogEvent(LogLevel.WARN, System.currentTimeMillis(), LoggerImpl.abbreviateLoggerName(ActionLogger.class.getCanonicalName()), "reached max holding size of trace log, please contact arch team to split big task into smaller batch", null, null);
            logWriter.writeTraceLog(traceWriter, warning);

            if (logForwarder != null)
                logForwarder.queueTraceLog(log, Lists.newArrayList(warning));
        }

        logWriter.writeTraceLog(traceWriter, event);

        if (logForwarder != null && size <= MAX_HOLD_SIZE) {    // not forward trace to queue if more than max lines.
            logForwarder.queueTraceLog(log, Lists.newArrayList(event));
        }
    }
}
