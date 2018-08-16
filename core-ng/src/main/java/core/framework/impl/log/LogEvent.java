package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.impl.log.marker.ErrorCodeMarker;
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

    String logMessage(LogFilter filter) {
        if (logMessage == null) {
            var builder = new StringBuilder(256);
            builder.append(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(time)))
                   .append(" [")
                   .append(thread)
                   .append("] ")
                   .append(level.name())
                   .append(' ')
                   .append(logger)
                   .append(" - ");
            if (marker != null) builder.append('[').append(marker.getName()).append("] ");
            builder.append(filter.format(message, arguments)).append(System.lineSeparator());
            if (exception != null) builder.append(Exceptions.stackTrace(exception));
            logMessage = builder.toString();
        }
        return logMessage;
    }

    String message() {  // only be called for error message, it assumes warn/error message won't contains sensitive data which should not be logged as warn/error in first place
        if (arguments == null || arguments.length == 0) return message;
        return Strings.format(message, arguments);
    }

    String errorCode() {
        if (marker instanceof ErrorCodeMarker) return marker.getName();
        return null;
    }
}
