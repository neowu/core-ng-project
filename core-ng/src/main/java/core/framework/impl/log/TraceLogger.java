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

import static core.framework.api.util.Files.createDir;
import static java.nio.file.Files.createFile;

/**
 * @author neo
 */
public final class TraceLogger {
    private static final DateTimeFormatter TRACE_LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static TraceLogger console() {
        return new TraceLogger(null);
    }

    public static TraceLogger file(Path traceLogPath) {
        createDir(traceLogPath);
        return new TraceLogger(traceLogPath);
    }

    private final PrintStream errorLogger = System.err;
    private final Path traceLogPath;
    private boolean console;

    TraceLogger(Path traceLogPath) {
        this.traceLogPath = traceLogPath;
    }

    void write(ActionLog log) {
        if (!log.flushTraceLog()) return;

        Writer writer = createWriter(log);
        try {
            for (LogEvent event : log.events) {
                String message = event.logMessage();
                writer.write(message);
            }
        } catch (IOException e) {
            errorLogger.println("failed to write trace log, error=" + Exceptions.stackTrace(e));
        } finally {
            close(writer);
        }
    }

    private void close(Writer writer) {
        try {
            if (console) {
                writer.flush();     // do not close System.err (when traceLogPath is null)
            } else {
                writer.close();
            }
        } catch (IOException e) {
            errorLogger.println("failed to close writer, error=" + Exceptions.stackTrace(e));
        }
    }

    private Writer createWriter(ActionLog log) {
        if (traceLogPath != null) {
            try {
                String logPath = traceLogFilePath(traceLogPath.toString(), LocalDateTime.ofInstant(log.date, ZoneId.systemDefault()), log.action, log.id);
                log.context.put("logPath", logPath);    // not log as event but add value to context, no need to see logPath in trace
                Path path = Paths.get(logPath).toAbsolutePath();
                createDir(path.getParent());
                createFile(path);
                return Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                errorLogger.println("failed to create trace log file, error=" + Exceptions.stackTrace(e));
            }
        }
        console = true;
        return new BufferedWriter(new OutputStreamWriter(System.err, Charsets.UTF_8));
    }

    String traceLogFilePath(String logDirectory, LocalDateTime date, String action, String id) {
        String sequence = Randoms.alphaNumeric(5);
        return logDirectory + "/" + action + "/" + TRACE_LOG_DATE_FORMAT.format(date) + "." + id + "." + sequence + ".log";
    }
}
