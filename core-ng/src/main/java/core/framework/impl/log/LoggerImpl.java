package core.framework.impl.log;

import core.framework.api.util.Strings;
import org.slf4j.Marker;

import java.io.PrintStream;

/**
 * @author neo
 */
final class LoggerImpl extends AbstractLogger {
    static String abbreviateLoggerName(String name) {
        String[] tokens = Strings.split(name, '.');
        StringBuilder builder = new StringBuilder();
        int total = tokens.length >= 4 ? 3 : tokens.length - 1;
        int index = 1;
        for (String token : tokens) {
            if (index > 1) builder.append('.');
            if (index <= total && token.length() >= 1) {
                builder.append(token.charAt(0));
            } else {
                builder.append(token);
            }
            index++;
        }
        return builder.toString();
    }

    private final PrintStream output = System.out;
    private final LogManager logManager;
    private final LogLevel traceLevel;
    private final String logger;

    public LoggerImpl(String name, LogManager logManager, LogLevel traceLevel) {
        super(name);
        this.logger = abbreviateLoggerName(name);
        this.logManager = logManager;
        this.traceLevel = traceLevel;
    }

    @Override
    void log(Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception) {
        if (message == null) throw new Error("log message must not be null");

        if (level.value >= traceLevel.value) {
            LogEvent event = new LogEvent(logger, marker, level, message, arguments, exception);
            logManager.process(event);

            if (level.value >= LogLevel.INFO.value) {
                output.print(event.logMessage());
            }
        }
    }
}
