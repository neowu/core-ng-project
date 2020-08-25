package core.framework.internal.log.appender;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.log.message.StatMessage;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public final class ConsoleAppender implements LogAppender {
    private static final String LOG_SPLITTER = " | ";

    private final PrintStream stdout = System.out;
    private final PrintStream stderr = System.err;

    @Override
    public void append(ActionLogMessage message) {
        stdout.println(message(message));

        if (message.traceLog != null) {
            stderr.println(message.traceLog);
        }
    }

    @Override
    public void append(StatMessage message) {
        stdout.println(message(message));
    }

    String message(ActionLogMessage log) {
        var format = new DecimalFormat();   // according to benchmark, create DecimalFormat every time is faster than String.format("%.9", value)

        var builder = new StringBuilder(512);
        builder.append(DateTimeFormatter.ISO_INSTANT.format(log.date))
                .append(LOG_SPLITTER).append(log.result)
                .append(LOG_SPLITTER).append("elapsed=").append(format.format(log.elapsed.longValue()))
                .append(LOG_SPLITTER).append("id=").append(log.id)
                .append(LOG_SPLITTER).append("action=").append(log.action);

        if (log.correlationIds != null) {
            builder.append(LOG_SPLITTER).append("correlation_id=");
            appendList(builder, log.correlationIds);
        }
        String errorCode = log.errorCode;
        if (errorCode != null) {
            builder.append(LOG_SPLITTER).append("error_code=").append(errorCode)
                    .append(LOG_SPLITTER).append("error_message=").append(filterLineSeparator(log.errorMessage));
        }
        builder.append(LOG_SPLITTER).append("cpu_time=").append(format.format(log.cpuTime.longValue()));

        for (Map.Entry<String, List<String>> entry : log.context.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
                builder.append(LOG_SPLITTER).append(key).append('=').append(filterLineSeparator(value));
            }
        }
        if (log.clients != null) {
            builder.append(LOG_SPLITTER).append("client=");
            appendList(builder, log.clients);
        }
        if (log.refIds != null) {
            builder.append(LOG_SPLITTER).append("ref_id=");
            appendList(builder, log.refIds);
        }
        if (log.stats != null) {
            for (Map.Entry<String, Double> entry : log.stats.entrySet()) {
                builder.append(LOG_SPLITTER).append(entry.getKey()).append('=').append(format.format(entry.getValue()));
            }
        }
        for (Map.Entry<String, PerformanceStatMessage> entry : log.performanceStats.entrySet()) {
            String key = entry.getKey();
            PerformanceStatMessage stat = entry.getValue();
            builder.append(LOG_SPLITTER).append(key).append("_count=").append(stat.count);
            if (stat.readEntries != null) builder.append(LOG_SPLITTER).append(key).append("_reads=").append(stat.readEntries);
            if (stat.writeEntries != null) builder.append(LOG_SPLITTER).append(key).append("_writes=").append(stat.writeEntries);
            builder.append(LOG_SPLITTER).append(key).append("_elapsed=").append(format.format(stat.totalElapsed));
        }
        return builder.toString();
    }

    String message(StatMessage message) {
        var builder = new StringBuilder(512);
        builder.append(DateTimeFormatter.ISO_INSTANT.format(message.date))
                .append(LOG_SPLITTER).append(message.result);

        String errorCode = message.errorCode;
        if (errorCode != null) {
            builder.append(LOG_SPLITTER).append("error_code=").append(errorCode)
                    .append(LOG_SPLITTER).append("error_message=").append(message.errorMessage);
        }

        var format = new DecimalFormat();
        for (Map.Entry<String, Double> entry : message.stats.entrySet()) {
            builder.append(LOG_SPLITTER)
                    .append(entry.getKey()).append('=')
                    .append(format.format(entry.getValue()));
        }

        if (message.info != null) {
            for (Map.Entry<String, String> entry : message.info.entrySet()) {
                builder.append(LOG_SPLITTER)
                        .append(entry.getKey()).append('=')
                        .append(entry.getValue());
            }
        }

        return builder.toString();
    }

    private void appendList(StringBuilder builder, List<String> items) {
        int index = 0;
        for (String item : items) {
            if (index > 0) builder.append(',');
            builder.append(item);
            index++;
        }
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
