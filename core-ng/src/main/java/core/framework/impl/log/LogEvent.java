package core.framework.impl.log;

import core.framework.impl.log.marker.ErrorCodeMarker;
import core.framework.util.Exceptions;
import org.slf4j.Marker;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author neo
 */
final class LogEvent {
    final LogLevel level;

    private final long time = System.nanoTime();
    private final String logger;
    private final Marker marker;
    private final String message;
    private final Object[] arguments;
    private final Throwable exception;

    LogEvent(String logger, Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception) {
        this.level = level;
        this.marker = marker;
        this.logger = logger;
        this.message = message;
        this.arguments = arguments;
        this.exception = exception;
    }

    String message() {  // only be called for error message
        if (arguments == null || arguments.length == 0) return message;     // most of case, message is from exception, and without no arguments

        var builder = new StringBuilder(256);
        LogManager.FILTER.append(builder, message, arguments);
        return builder.toString();
    }

    String errorCode() {
        if (marker instanceof ErrorCodeMarker) return marker.getName();
        return null;
    }

    String info() {
        var now = Instant.now();
        var builder = new StringBuilder(256);
        builder.append(DateTimeFormatter.ISO_INSTANT.format(now))
               .append(" [").append(Thread.currentThread().getName()).append("] ")
               .append(level.name())
               .append(' ')
               .append(logger)
               .append(" - ");
        if (marker != null) builder.append('[').append(marker.getName()).append("] ");
        LogManager.FILTER.append(builder, message, arguments);
        builder.append(System.lineSeparator());
        if (exception != null) builder.append(Exceptions.stackTrace(exception));
        return builder.toString();
    }

    void appendTrace(StringBuilder builder, long startTime) {
        appendDuration(builder, time - startTime);
        builder.append(' ');
        if (level != LogLevel.DEBUG) builder.append(level.name()).append(' ');
        builder.append(logger)
               .append(" - ");
        if (marker != null) builder.append('[').append(marker.getName()).append("] ");
        LogManager.FILTER.append(builder, message, arguments);
        builder.append(System.lineSeparator());
        if (exception != null) builder.append(Exceptions.stackTrace(exception));
    }

    void appendDuration(StringBuilder builder, long durationInNanos) {
        long seconds = durationInNanos / 1000000000;

        String minutePart = String.valueOf(seconds / 60);
        if (minutePart.length() < 2) builder.append('0');
        builder.append(minutePart).append(':');

        String secondPart = String.valueOf(seconds % 60);
        if (secondPart.length() < 2) builder.append('0');
        builder.append(secondPart).append('.');

        String nanoPart = String.valueOf(durationInNanos % 1000000000);
        int padding = 9 - nanoPart.length();
        for (int i = 0; i < padding; i++) {
            builder.append('0');
        }
        builder.append(nanoPart);
    }
}
