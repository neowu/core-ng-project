package core.framework.internal.log;

import core.framework.util.Strings;
import org.slf4j.Marker;

import java.io.PrintStream;

/**
 * @author neo
 */
public final class LoggerImpl extends AbstractLogger {
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    static String abbreviateLoggerName(String name) {
        String[] tokens = Strings.split(name, '.');
        var builder = new StringBuilder();
        int abbrCount = tokens.length <= 4 ? tokens.length - 1 : tokens.length - 2;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) builder.append('.');
            if (i < abbrCount && !token.isEmpty()) {
                builder.append(token.charAt(0));
            } else {
                builder.append(token);
            }
        }
        return builder.toString();
    }

    private final LogLevel infoLevel;
    private final LogLevel traceLevel;

    LoggerImpl(String name, LogLevel infoLevel, LogLevel traceLevel) {
        super(abbreviateLoggerName(name));
        this.infoLevel = infoLevel;
        this.traceLevel = traceLevel;
    }

    @Override
    public void log(Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception) {
        if (level.value >= traceLevel.value) {
            var event = new LogEvent(name, marker, level, message, arguments, exception);

            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            if (actionLog != null) actionLog.process(event);    // logManager.begin() may not be called

            if (level.value >= infoLevel.value) {
                write(event);
            }
        }
    }

    private void write(LogEvent event) {
        String message = event.info();
        if (event.level.value >= LogLevel.WARN.value)
            STDERR.print(message);
        else
            STDOUT.print(message);
    }
}
