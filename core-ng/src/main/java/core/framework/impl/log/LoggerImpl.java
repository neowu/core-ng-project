package core.framework.impl.log;

import java.io.PrintStream;

/**
 * @author neo
 */
final class LoggerImpl extends AbstractLogger {
    static String abbreviateLoggerName(String name) {
        String[] tokens = name.split("\\.");
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
    private final LogLevel logLevel;
    private final LogLevel traceLevel;
    private final String logger;

    public LoggerImpl(String name, LogManager logManager, LogLevel logLevel, LogLevel traceLevel) {
        super(name);
        this.logger = abbreviateLoggerName(name);
        this.logManager = logManager;
        this.logLevel = logLevel;
        this.traceLevel = traceLevel;
    }

    @Override
    void log(LogLevel level, String message, Object[] arguments, Throwable exception) {
        if (level.value >= traceLevel.value) {
            LogEvent event = new LogEvent(level, System.currentTimeMillis(), logger, message, arguments, exception);
            logManager.process(event);

            if (level.value >= logLevel.value) {
                output.print(event.logMessage());
            }
        }
    }
}
