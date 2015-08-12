package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Randoms;
import core.framework.api.util.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author neo
 */
public class LogWriter {
    private static final DateTimeFormatter TRACE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String LOG_SPLITTER = " | ";
    private Writer actionLogWriter = new BufferedWriter(new OutputStreamWriter(System.out, Charsets.UTF_8));
    private Path traceLogPath;
    private final PrintStream fallbackLogger = System.err;

    public void actionLogPath(Path actionLogPath) {
        Path path = actionLogPath.toAbsolutePath();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.createFile(path);
            if (!Files.isWritable(path)) throw Exceptions.error("action log file is not writable, path={}", path);
            actionLogWriter = Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void traceLogPath(Path traceLogPath) {
        try {
            Files.createDirectories(traceLogPath);
            this.traceLogPath = traceLogPath;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void close() {
        try {
            actionLogWriter.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Writer createTraceWriter(ActionLog log) {
        if (traceLogPath != null) {
            try {
                String traceLogPath = traceLogFilePath(this.traceLogPath.toString(), LocalDateTime.ofInstant(log.startTime, ZoneId.systemDefault()), log.action, log.id);
                log.context("logPath", traceLogPath);
                Path path = Paths.get(traceLogPath).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                return Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                fallbackLogger.println("failed to create trace log file, error=" + Exceptions.stackTrace(e));
            }
        }
        return new BufferedWriter(new OutputStreamWriter(System.err, Charsets.UTF_8));
    }

    void writeTraceLog(Writer writer, LogEvent event) {
        String message = event.logMessage();
        try {
            writer.write(message);
        } catch (IOException e) {
            fallbackLogger.println("failed to write log, log=" + message + ", error=" + Exceptions.stackTrace(e));
        }
    }

    void closeTraceLogWriter(Writer writer) {
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

    String traceLogFilePath(String logPath, LocalDateTime date, String action, String id) {
        String sequence = Randoms.alphaNumeric(5);

        return Strings.format("{}/{}/{}.{}.{}.log",
            logPath,
            action,
            TRACE_LOG_DATE_FORMAT.format(date),
            id,
            sequence);
    }

    void writeActionLog(ActionLog log) {
        String actionLogMessage = actionLogMessage(log);
        try {
            actionLogWriter.write(actionLogMessage);
            actionLogWriter.flush();
        } catch (IOException e) {
            fallbackLogger.println("failed to write action log, log=" + actionLogMessage + ", error=" + Exceptions.stackTrace(e));
        }
    }

    String actionLogMessage(ActionLog log) {
        StringBuilder builder = new StringBuilder();
        builder.append(DateTimeFormatter.ISO_INSTANT.format(log.startTime))
            .append(LOG_SPLITTER)
            .append(log.result())
            .append(LOG_SPLITTER)
            .append("elapsed=")
            .append(log.elapsed)
            .append(LOG_SPLITTER)
            .append("id=")
            .append(log.id)
            .append(LOG_SPLITTER)
            .append("action=")
            .append(log.action);

        if (log.refId != null) {
            builder.append(LOG_SPLITTER)
                .append("refId=")
                .append(log.refId);
        }

        if (log.exceptionClass != null) {
            builder.append(LOG_SPLITTER)
                .append("errorMessage=")
                .append(filterLineSeparator(log.errorMessage))
                .append(LOG_SPLITTER)
                .append("exceptionClass=")
                .append(log.exceptionClass.getCanonicalName());
        }

        for (Map.Entry<String, String> entry : log.context.entrySet()) {
            builder.append(LOG_SPLITTER)
                .append(entry.getKey())
                .append('=')
                .append(filterLineSeparator(entry.getValue()));
        }

        for (Map.Entry<String, PerformanceStat> entry : log.performanceStats.entrySet()) {
            String action = entry.getKey();
            PerformanceStat tracking = entry.getValue();
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
}
