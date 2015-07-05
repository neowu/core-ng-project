package core.framework.impl.log;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
class LogEvent {
    static final String LINE_SEPARATOR;

    static {
        String systemLineSeparator = System.getProperty("line.separator");
        if (systemLineSeparator == null)
            LINE_SEPARATOR = "\n";
        else
            LINE_SEPARATOR = systemLineSeparator;
    }

    final LogLevel level;
    final long time;
    final String logger;
    final String message;
    final Object[] arguments;
    final Throwable exception;

    String logMessage;

    public LogEvent(LogLevel level, long time, String logger, String message, Object[] arguments, Throwable exception) {
        this.level = level;
        this.time = time;
        this.logger = logger;
        this.message = message;
        this.arguments = arguments;
        this.exception = exception;
    }

    public String logMessage() {
        if (logMessage == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(time)))
                .append(" [")
                .append(Thread.currentThread().getName()).append("] ")
                .append(level.name())
                .append(' ')
                .append(logger)
                .append(" - ");

            if (arguments == null)
                builder.append(message);
            else
                builder.append(Strings.format(message, arguments));

            builder.append(LINE_SEPARATOR);
            if (exception != null)
                builder.append(Exceptions.stackTrace(exception));

            logMessage = builder.toString();
        }
        return logMessage;
    }
}
