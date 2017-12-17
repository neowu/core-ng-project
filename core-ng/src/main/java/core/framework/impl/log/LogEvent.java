package core.framework.impl.log;

import core.framework.impl.log.marker.ErrorCodeMarker;
import core.framework.log.MessageFilter;
import core.framework.util.Exceptions;
import core.framework.util.Strings;
import org.slf4j.Marker;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
final class LogEvent {
    final LogLevel level;
    private final String thread;
    private final String logger;
    private final Marker marker;
    private final long time = System.currentTimeMillis();
    private final String message;
    private final Object[] arguments;
    private final Throwable exception;
    MessageFilter filter;
    private String logMessage;

    LogEvent(String logger, Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception) {
        this.level = level;
        this.marker = marker;
        this.logger = logger;
        this.message = message;
        this.arguments = arguments;
        this.exception = exception;
        thread = Thread.currentThread().getName();
    }

    String logMessage() {
        if (logMessage == null) {
            StringBuilder builder = new StringBuilder(256);
            builder.append(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(time)))
                .append(" [")
                .append(thread)
                .append("] ")
                .append(level.name())
                .append(' ')
                .append(logger)
                .append(" - ");

            if (marker != null) {
                builder.append('[').append(marker.getName()).append("] ");
            }

            builder.append(message())
                   .append(System.lineSeparator());
            if (exception != null)
                builder.append(Exceptions.stackTrace(exception));

            logMessage = builder.toString();
        }
        return logMessage;
    }

    String message() {
        String message;
        if (arguments == null) {
            message = this.message;    // log message can be null, e.g. message of NPE
        } else {
            message = Strings.format(this.message, arguments);
        }
        try {
            if (filter != null) return filter.filter(logger, message);
        } catch (Throwable e) {
            return "failed to filter log message, error=" + e.getMessage() + System.lineSeparator() + Exceptions.stackTrace(e);
        }
        return message;
    }

    String errorCode() {
        if (marker instanceof ErrorCodeMarker) return marker.getName();
        return null;
    }
}
