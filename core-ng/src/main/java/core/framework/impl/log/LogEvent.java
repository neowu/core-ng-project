package core.framework.impl.log;

import core.framework.api.log.Markers;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Strings;
import core.framework.impl.log.marker.ErrorTypeMarker;
import org.slf4j.Marker;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
final class LogEvent {
    final LogLevel level;
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

            if (marker != null) {
                builder.append('[').append(marker.getName()).append("] ");
            }

            builder.append(message());

            builder.append(System.lineSeparator());
            if (exception != null)
                builder.append(Exceptions.stackTrace(exception));

            logMessage = builder.toString();
        }
        return logMessage;
    }

    String message() {
        if (arguments == null)
            return message;
        else
            return Strings.format(message, arguments);
    }

    boolean trace() {
        return marker == Markers.TRACE;
    }

    String errorType() {
        if (marker instanceof ErrorTypeMarker) return marker.getName();
        return null;
    }

    boolean isWarningOrError() {
        return level.value >= LogLevel.WARN.value;
    }
}
