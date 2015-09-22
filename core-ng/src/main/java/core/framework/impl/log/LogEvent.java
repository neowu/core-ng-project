package core.framework.impl.log;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
final class LogEvent {
    final LogLevel level;
    final long time;
    final String logger;
    final String message;
    final Object[] arguments;
    final Throwable exception;

    String logMessage;

    LogEvent(LogLevel level, long time, String logger, String message, Object[] arguments, Throwable exception) {
        this.level = level;
        this.time = time;
        this.logger = logger;
        this.message = message;
        this.arguments = arguments;
        this.exception = exception;
    }

    String logMessage() {
        if (logMessage == null) {
            StringBuilder builder = new StringBuilder(64);
            builder.append(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(time)))
                .append(" [")
                .append(Thread.currentThread().getName())
                .append("] ")
                .append(level.name())
                .append(' ')
                .append(logger)
                .append(" - ");

            if (arguments == null)
                builder.append(message);
            else
                builder.append(Strings.format(message, arguments));

            builder.append(System.lineSeparator());
            if (exception != null)
                builder.append(Exceptions.stackTrace(exception));

            logMessage = builder.toString();
        }
        return logMessage;
    }
}
