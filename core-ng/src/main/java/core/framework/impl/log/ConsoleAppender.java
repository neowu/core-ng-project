package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author neo
 */
public final class ConsoleAppender implements Appender {
    private static final String LOG_SPLITTER = " | ";

    private final PrintStream stdout = System.out;
    private final PrintStream stderr = System.err;

    @Override
    public void append(ActionLog log, LogFilter filter) {
        stdout.println(message(log));

        if (log.flushTraceLog()) {
            for (LogEvent event : log.events) {
                stderr.print(event.logMessage(filter));
            }
        }
    }

    String message(ActionLog log) {
        StringBuilder builder = new StringBuilder(256);
        builder.append(DateTimeFormatter.ISO_INSTANT.format(log.date))
               .append(LOG_SPLITTER).append(log.result())
               .append(LOG_SPLITTER).append("elapsed=").append(log.elapsed)
               .append(LOG_SPLITTER).append("id=").append(log.id)
               .append(LOG_SPLITTER).append("action=").append(log.action);

        if (log.refId != null)
            builder.append(LOG_SPLITTER).append("refId=").append(log.refId);

        String errorCode = log.errorCode();
        if (errorCode != null) {
            builder.append(LOG_SPLITTER).append("errorCode=").append(errorCode)
                   .append(LOG_SPLITTER).append("errorMessage=").append(filterLineSeparator(log.errorMessage));
        }
        builder.append(LOG_SPLITTER).append("cpuTime=").append(log.cpuTime);

        for (Map.Entry<String, String> entry : log.context.entrySet()) {
            builder.append(LOG_SPLITTER).append(entry.getKey()).append('=').append(filterLineSeparator(entry.getValue()));
        }

        for (Map.Entry<String, PerformanceStat> entry : log.performanceStats.entrySet()) {
            String key = entry.getKey();
            PerformanceStat tracking = entry.getValue();
            builder.append(LOG_SPLITTER).append(key).append("Count=").append(tracking.count);
            if (tracking.readEntries != null) builder.append(LOG_SPLITTER).append(key).append("Reads=").append(tracking.readEntries);
            if (tracking.writeEntries != null) builder.append(LOG_SPLITTER).append(key).append("Writes=").append(tracking.writeEntries);
            builder.append(LOG_SPLITTER).append(key).append("Elapsed=").append(tracking.totalElapsed);
        }

        return builder.toString();
    }

    String filterLineSeparator(String value) {
        if (value == null) return "";
        int length = value.length();
        var builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == '\n' || ch == '\r') builder.append(' ');
            else builder.append(ch);
        }
        return builder.toString();
    }
}
