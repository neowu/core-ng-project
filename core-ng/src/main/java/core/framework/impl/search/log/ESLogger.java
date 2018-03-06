package core.framework.impl.search.log;

import core.framework.impl.log.LogLevel;
import core.framework.impl.log.LoggerImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;

public class ESLogger extends AbstractLogger {
    private static final long serialVersionUID = -4341238245729493821L;
    private final LoggerImpl logger;

    public ESLogger(String name, MessageFactory messageFactory, LoggerImpl logger) {
        super(name, messageFactory);
        this.logger = logger;
    }

    @Override
    public Level getLevel() {
        return Level.INFO;      // only process info level for elasticsearch
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message data, Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence data, Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object data, Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String data) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String data, Object... p1) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return isEnabled(level);
    }

    @SuppressWarnings("ParameterNumber")
    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String data, Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level) {
        return level.getStandardLevel().intLevel() <= Level.INFO.intLevel();
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable exception) {
        logger.log(null, logLevel(level), message.getFormattedMessage(), message.getParameters(), exception);
    }

    private LogLevel logLevel(Level level) {
        switch (level.getStandardLevel()) {
            case INFO:
                return LogLevel.INFO;
            case WARN:
                return LogLevel.WARN;
            case ERROR:
            case FATAL:
                return LogLevel.ERROR;
            default:
                return LogLevel.DEBUG;
        }
    }
}
