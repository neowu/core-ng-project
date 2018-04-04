package core.framework.impl.log;

import core.framework.util.Strings;
import org.slf4j.Marker;

import java.io.PrintStream;

/**
 * @author neo
 */
public final class LoggerImpl extends AbstractLogger {
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

    private final PrintStream stdout = System.out;
    private final PrintStream stderr = System.err;
    private final LogManager logManager;
    private final LogLevel infoLevel;
    private final LogLevel traceLevel;
    private final String logger;

    LoggerImpl(String name, LogManager logManager, LogLevel infoLevel, LogLevel traceLevel) {
        super(name);
        this.logger = abbreviateLoggerName(name);
        this.logManager = logManager;
        this.infoLevel = infoLevel;
        this.traceLevel = traceLevel;
    }

    @Override
    public void log(Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception) {
        if (level.value >= traceLevel.value) {
            LogEvent event = new LogEvent(logger, marker, level, message, arguments, exception, logManager.filter);
            logManager.process(event);

            if (level.value >= infoLevel.value) {
                write(event);
            }
        }
    }

    private void write(LogEvent event) {
        String message = event.logMessage();
        if (event.level.value >= LogLevel.WARN.value)
            stderr.print(message);
        else
            stdout.print(message);
    }
}
