package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Randoms;
import core.framework.api.util.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class ActionLog {
    private static final int MAX_HOLD_SIZE = 5000;
    private static final String LOG_SPLITTER = " | ";
    private static final DateTimeFormatter TRACE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final PrintStream fallbackLogger = System.err;

    private List<LogEvent> events = new LinkedList<>();
    final Map<String, String> context = new LinkedHashMap<>();
    final Map<String, TimeTracking> tracking = Maps.newHashMap();
    private final Instant startTime = Instant.now();
    private LogLevel result = LogLevel.INFO;

    private Writer writer;
    private final Path traceLogPath;

    public ActionLog(Path traceLogPath) {
        this.traceLogPath = traceLogPath;
    }

    public void process(LogEvent event) {
        LogLevel level = event.level;
        if (level.value > result.value) result = level;

        if (events != null) {
            events.add(event);
            if (event.level.value >= LogLevel.WARN.value || events.size() > MAX_HOLD_SIZE) {
                flushTraceLogs();
                events = null;
            }
        } else {
            write(event);
        }
    }

    public void end(Writer actionLogWriter) {
        writeActionLog(actionLogWriter);

        if (writer != null) {
            closeTraceLogWriter();
        }
    }

    private void writeActionLog(Writer actionLogWriter) {
        String actionLogMessage = actionLogMessage();
        try {
            actionLogWriter.write(actionLogMessage);
            actionLogWriter.flush();
        } catch (IOException e) {
            fallbackLogger.println("failed to write action log, log=" + actionLogMessage + ", error=" + Exceptions.stackTrace(e));
        }
    }

    private void closeTraceLogWriter() {
        try {
            if (traceLogPath == null) {
                writer.flush();     // do not close System.err (when traceLogPath is null)
            } else {
                writer.close();
            }
        } catch (IOException e) {
            fallbackLogger.println("failed to flush trace writer, error=" + Exceptions.stackTrace(e));
        }
    }

    String actionLogMessage() {
        long elapsed = Duration.between(startTime, Instant.now()).toMillis();

        StringBuilder builder = new StringBuilder();
        builder.append(DateTimeFormatter.ISO_INSTANT.format(startTime))
            .append(LOG_SPLITTER)
            .append(result == LogLevel.INFO ? "OK" : result)
            .append(LOG_SPLITTER)
            .append("elapsed=")
            .append(elapsed);

        for (Map.Entry<String, String> entry : context.entrySet()) {
            builder.append(LOG_SPLITTER)
                .append(entry.getKey())
                .append('=')
                .append(filterLineSeparator(entry.getValue()));
        }

        for (Map.Entry<String, TimeTracking> entry : tracking.entrySet()) {
            String action = entry.getKey();
            TimeTracking tracking = entry.getValue();
            builder.append(LOG_SPLITTER)
                .append(action).append("Count=").append(tracking.count)
                .append(LOG_SPLITTER)
                .append(action).append("ElapsedTime=").append(tracking.totalElapsedTime);
        }

        builder.append(LogEvent.LINE_SEPARATOR);

        return builder.toString();
    }

    String filterLineSeparator(String value) {
        if (value == null) return "";
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\n' || ch == '\r') builder.append(' ');
            else builder.append(ch);
        }
        return builder.toString();
    }

    private void flushTraceLogs() {
        if (writer == null)
            writer = createWriter();

        events.forEach(this::write);
    }

    void write(LogEvent event) {
        String message = event.logMessage();
        try {
            writer.write(message);
        } catch (IOException e) {
            fallbackLogger.println("failed to write log, log=" + message + ", error=" + Exceptions.stackTrace(e));
        }
    }

    private Writer createWriter() {
        if (traceLogPath != null) {
            try {
                String logFilePath = traceLogFilePath(traceLogPath.toString(), LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()), context.get("action"), context.get("requestId"));
                context.put("logPath", logFilePath);
                Path logFile = Paths.get(logFilePath).toAbsolutePath();
                Files.createDirectories(logFile.getParent());
                Files.createFile(logFile);
                return Files.newBufferedWriter(logFile, Charsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                fallbackLogger.println("failed to create trace log file, error=" + Exceptions.stackTrace(e));
            }
        }
        return new BufferedWriter(new OutputStreamWriter(System.err, Charsets.UTF_8));
    }

    String traceLogFilePath(String logPath, LocalDateTime date, String action, String requestId) {
        String sequence = Randoms.alphaNumeric(5);

        return Strings.format("{}/{}/{}.{}.{}.log",
            logPath,
            action == null ? "unassigned" : action,
            TRACE_LOG_DATE_FORMAT.format(date),
            requestId == null ? "unassigned" : requestId,
            sequence);
    }
}
