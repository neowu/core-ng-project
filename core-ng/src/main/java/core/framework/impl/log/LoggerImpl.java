package core.framework.impl.log;

import java.io.PrintStream;

/**
 * @author neo
 */
class LoggerImpl extends AbstractLogger {
    private final PrintStream output = System.out;
    private final ActionLogger actionLogger;
    private final LogLevel logLevel;
    private final LogLevel traceLevel;
    private final String logger;

    public LoggerImpl(String name, ActionLogger actionLogger, LogLevel logLevel, LogLevel traceLevel) {
        super(name);
        this.logger = abbreviateLoggerName(name);
        this.actionLogger = actionLogger;
        this.logLevel = logLevel;
        this.traceLevel = traceLevel;
    }

    String abbreviateLoggerName(String name) {
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

    @Override
    void log(LogLevel level, String message, Object[] arguments, Throwable exception) {
        if (level.value >= traceLevel.value) {
            LogEvent event = new LogEvent(level, System.currentTimeMillis(), logger, message, arguments, exception);
            actionLogger.process(event);

            if (level.value >= this.logLevel.value) {
                output.print(event.logMessage());
            }
        }
    }
}
