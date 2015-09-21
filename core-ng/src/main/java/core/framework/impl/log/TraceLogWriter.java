package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Randoms;

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

/**
 * @author neo
 */
public final class TraceLogWriter {
    private static final DateTimeFormatter TRACE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final Path traceLogPath;
    private final PrintStream fallbackLogger = System.err;

    public static TraceLogWriter console() {
        return new TraceLogWriter(null);
    }

    public static TraceLogWriter file(Path traceLogPath) {
        try {
            Files.createDirectories(traceLogPath);
            return new TraceLogWriter(traceLogPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TraceLogWriter(Path traceLogPath) {
        this.traceLogPath = traceLogPath;
    }

    Writer createWriter(ActionLog log) {
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

    void write(Writer writer, LogEvent event) {
        String message = event.logMessage();
        try {
            writer.write(message);
        } catch (IOException e) {
            fallbackLogger.println("failed to write log, log=" + message + ", error=" + Exceptions.stackTrace(e));
        }
    }

    void closeWriter(Writer writer) {
        try {
            if (traceLogPath == null) {
                writer.flush();     // do not close System.err (when traceLogPath is null)
            } else {
                writer.close();
            }
        } catch (IOException e) {
            fallbackLogger.println("failed to flush trace log writer, error=" + Exceptions.stackTrace(e));
        }
    }

    String traceLogFilePath(String logPath, LocalDateTime date, String action, String id) {
        String sequence = Randoms.alphaNumeric(5);

        return logPath + '/' + action + '/' + TRACE_LOG_DATE_FORMAT.format(date) + '.' + id + '.' + sequence + ".log";
    }
}
