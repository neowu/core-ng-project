package core.framework.impl.log;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * @author neo
 */
abstract class AbstractLogger implements Logger {
    private final String name;

    AbstractLogger(String name) {
        this.name = name;
    }

    abstract void log(Marker marker, LogLevel level, String message, Object[] arguments, Throwable exception);

    private void logWithOneArgument(Marker marker, LogLevel level, String format, Object arg) {
        if (arg instanceof Throwable)
            log(marker, level, format, null, (Throwable) arg);
        else
            log(marker, level, format, new Object[]{arg}, null);
    }

    private void logWithTwoArguments(Marker marker, LogLevel level, String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable)
            log(marker, level, format, new Object[]{arg1}, (Throwable) arg2);
        else
            log(marker, level, format, new Object[]{arg1, arg2}, null);
    }

    private void logWithArguments(Marker marker, LogLevel level, String format, Object... arguments) {
        // arguments length must be greater than 1, because there are same method with 2 arguments
        Object lastArgument = arguments[arguments.length - 1];
        if (lastArgument instanceof Throwable) {
            Object[] messageArguments = new Object[arguments.length - 1];
            System.arraycopy(arguments, 0, messageArguments, 0, arguments.length - 1);
            log(marker, level, format, messageArguments, (Throwable) lastArgument);
        } else {
            log(marker, level, format, arguments, null);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(String msg) {
    }

    @Override
    public void trace(String format, Object arg) {
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(String format, Object... arguments) {
    }

    @Override
    public void trace(String msg, Throwable t) {
    }

    @Override
    public void trace(Marker marker, String msg) {
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments) {
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(String msg) {
        log(null, LogLevel.DEBUG, msg, null, null);
    }

    @Override
    public void debug(String format, Object arg) {
        logWithOneArgument(null, LogLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logWithTwoArguments(null, LogLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logWithArguments(null, LogLevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log(null, LogLevel.DEBUG, msg, null, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        log(marker, LogLevel.DEBUG, msg, null, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logWithOneArgument(marker, LogLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logWithTwoArguments(marker, LogLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logWithArguments(marker, LogLevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.DEBUG, msg, null, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(String msg) {
        log(null, LogLevel.INFO, msg, null, null);
    }

    @Override
    public void info(String format, Object arg) {
        logWithOneArgument(null, LogLevel.INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logWithTwoArguments(null, LogLevel.INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logWithArguments(null, LogLevel.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        log(null, LogLevel.INFO, msg, null, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        log(marker, LogLevel.INFO, msg, null, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logWithOneArgument(marker, LogLevel.INFO, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logWithTwoArguments(marker, LogLevel.INFO, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logWithArguments(marker, LogLevel.INFO, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.INFO, msg, null, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(String msg) {
        log(null, LogLevel.WARN, msg, null, null);
    }

    @Override
    public void warn(String format, Object arg) {
        logWithOneArgument(null, LogLevel.WARN, format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logWithTwoArguments(null, LogLevel.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logWithArguments(null, LogLevel.WARN, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log(null, LogLevel.WARN, msg, null, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        log(marker, LogLevel.WARN, msg, null, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logWithOneArgument(marker, LogLevel.WARN, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logWithTwoArguments(marker, LogLevel.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logWithArguments(marker, LogLevel.WARN, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.WARN, msg, null, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(String msg) {
        log(null, LogLevel.ERROR, msg, null, null);
    }

    @Override
    public void error(String format, Object arg) {
        logWithOneArgument(null, LogLevel.ERROR, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logWithTwoArguments(null, LogLevel.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logWithArguments(null, LogLevel.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        log(null, LogLevel.ERROR, msg, null, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        log(marker, LogLevel.ERROR, msg, null, null);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logWithOneArgument(marker, LogLevel.ERROR, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logWithTwoArguments(marker, LogLevel.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logWithArguments(marker, LogLevel.ERROR, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.ERROR, msg, null, t);
    }
}
