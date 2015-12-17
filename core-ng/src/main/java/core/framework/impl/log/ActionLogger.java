package core.framework.impl.log;

import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author neo
 */
public final class ActionLogger {
    private static final String LOG_SPLITTER = " | ";

    public static ActionLogger console() {
        return new ActionLogger(new BufferedWriter(new OutputStreamWriter(System.out, Charsets.UTF_8)));
    }

    public static ActionLogger file(Path actionLogPath) {
        Path path = actionLogPath.toAbsolutePath();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.createFile(path);
            if (!Files.isWritable(path)) throw Exceptions.error("action log file is not writable, path={}", path);
            return new ActionLogger(Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Writer writer;
    private final PrintStream fallbackLogger = System.err;

    private ActionLogger(Writer writer) {
        this.writer = writer;
    }

    void write(ActionLog log) {
        String actionLogMessage = actionLogMessage(log);
        try {
            writer.write(actionLogMessage);
            writer.flush();
        } catch (IOException e) {
            fallbackLogger.println("failed to write action log, log=" + actionLogMessage + ", error=" + Exceptions.stackTrace(e));
        }
    }

    void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String actionLogMessage(ActionLog log) {
        StringBuilder builder = new StringBuilder(256);
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

        String errorCode = log.errorCode();
        if (errorCode != null) {
            builder.append(LOG_SPLITTER)
                .append("errorCode=")
                .append(errorCode)
                .append(LOG_SPLITTER)
                .append("errorMessage=")
                .append(filterLineSeparator(log.errorMessage));
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
                .append(action).append("ElapsedTime=").append(tracking.totalElapsed);
        }

        builder.append(System.lineSeparator());

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
