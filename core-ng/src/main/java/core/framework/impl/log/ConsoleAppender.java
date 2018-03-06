package core.framework.impl.log;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author neo
 */
public final class ConsoleAppender {
    private static final String LOG_SPLITTER = " | ";

    private final PrintStream stdout = System.out;
    private final PrintStream stderr = System.err;

    void write(ActionLog log) {
        String message = message(log);
        stdout.println(message);

        writeTrace(log);
    }

    private void writeTrace(ActionLog log) {
        if (!log.flushTraceLog()) return;

        for (LogEvent event : log.events) {
            String message = event.logMessage();
            stderr.print(message);
        }
    }

    String message(ActionLog log) {
        StringBuilder builder = new StringBuilder(256);
        builder.append(DateTimeFormatter.ISO_INSTANT.format(log.date))
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

        builder.append(LOG_SPLITTER).append("cpuTime=").append(log.cpuTime);

        for (Map.Entry<String, String> entry : log.context.entrySet()) {
            builder.append(LOG_SPLITTER)
                   .append(entry.getKey())
                   .append('=')
                   .append(filterLineSeparator(entry.getValue()));
        }

        for (Map.Entry<String, PerformanceStat> entry : log.performanceStats.entrySet()) {
            String key = entry.getKey();
            PerformanceStat tracking = entry.getValue();
            builder.append(LOG_SPLITTER)
                   .append(key).append("Count=").append(tracking.count)
                   .append(LOG_SPLITTER)
                   .append(key).append("ElapsedTime=").append(tracking.totalElapsed);
        }

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
