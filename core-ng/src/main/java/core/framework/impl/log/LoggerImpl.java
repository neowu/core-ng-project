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
        var builder = new StringBuilder();
        int abbrCount = tokens.length <= 4 ? tokens.length - 1 : tokens.length - 2;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) builder.append('.');
            if (i < abbrCount && token.length() >= 1) {
                builder.append(token.charAt(0));
            } else {
                builder.append(token);
            }
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
            var event = new LogEvent(logger, marker, level, message, arguments, exception);
            ActionLog actionLog = logManager.currentActionLog();
            if (actionLog != null) actionLog.process(event);    // logManager.begin() may not be called

            if (level.value >= infoLevel.value) {
                write(event);
            }
        }
    }

    private void write(LogEvent event) {
        String message = event.logMessage(logManager.filter);
        if (event.level.value >= LogLevel.WARN.value)
            stderr.print(message);
        else
            stdout.print(message);
    }
}
