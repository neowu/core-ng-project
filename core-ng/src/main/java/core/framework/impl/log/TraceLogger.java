package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Randoms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * @author neo
 */
class TraceLogger {
    private static final DateTimeFormatter TRACE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final int MAX_HOLD_SIZE = 5000;
    private static final String LOGGER = LoggerImpl.abbreviateLoggerName(ActionLogger.class.getCanonicalName());

    private final PrintStream fallbackLogger = System.err;

    private final Path traceLogPath;
    private final ActionLog actionLog;
    private final LogForwarder logForwarder;
    private List<LogEvent> events = new LinkedList<>();
    private int size = 0;
    private Writer writer;

    TraceLogger(Path traceLogPath, ActionLog actionLog, LogForwarder logForwarder) {
        this.traceLogPath = traceLogPath;
        this.actionLog = actionLog;
        this.logForwarder = logForwarder;
    }

    void process(LogEvent event) {
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

    void close() {
        if (writer != null) {
            try {
                if (traceLogPath == null) {
                    writer.flush();     // do not close System.err (when traceLogPath is null)
                } else {
                    writer.close();
                }
            } catch (IOException e) {
                fallbackLogger.println("failed to flush trace log, error=" + Exceptions.stackTrace(e));
            }
        }
    }

    private void flushTraceLogs() {
        writer = createWriter();

        for (LogEvent event : events) {
            write(event);
        }

        if (logForwarder != null) {
            logForwarder.forwardTraceLog(actionLog, events);
        }
    }

    void writeTraceLog(LogEvent event) {
        if (size == MAX_HOLD_SIZE + 1) {
            actionLog.updateResult(LogLevel.WARN);
            LogEvent warning = new LogEvent(LogLevel.WARN, System.currentTimeMillis(), LOGGER, "reached max holding size of trace log, please contact arch team to split big task into smaller batch", null, null);
            write(warning);
            if (logForwarder != null) logForwarder.forwardTraceLog(actionLog, warning);
        }

        write(event);

        if (logForwarder != null && size <= MAX_HOLD_SIZE) {    // not forward trace to queue if more than max lines.
            logForwarder.forwardTraceLog(actionLog, event);
        }
    }

    Writer createWriter() {
        if (traceLogPath != null) {
            try {
                String logPath = traceLogFilePath(traceLogPath.toString(), LocalDateTime.ofInstant(actionLog.startTime, ZoneId.systemDefault()), actionLog.action, actionLog.id);
                actionLog.context("logPath", logPath);
                Path path = Paths.get(logPath).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                return Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                fallbackLogger.println("failed to create trace log file, error=" + Exceptions.stackTrace(e));
                // fall back to console writer as below
            }
        }
        return new BufferedWriter(new OutputStreamWriter(System.err, Charsets.UTF_8));
    }

    void write(LogEvent event) {
        String message = event.logMessage();
        try {
            writer.write(message);
        } catch (IOException e) {
            fallbackLogger.println("failed to write log, log=" + message + ", error=" + Exceptions.stackTrace(e));
        }
    }

    String traceLogFilePath(String logDirectory, LocalDateTime date, String action, String id) {
        String sequence = Randoms.alphaNumeric(5);

        return logDirectory + "/" + action + "/" + TRACE_LOG_DATE_FORMAT.format(date) + "." + id + "." + sequence + ".log";
    }
}
