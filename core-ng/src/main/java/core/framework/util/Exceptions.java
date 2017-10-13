package core.framework.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author neo
 */
public final class Exceptions {
    public static String stackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public static Error error(String pattern, Object... arguments) {    // follow convention of logger, if last arg is exception, make it cause
        if (arguments.length > 0) {
            Object lastArgument = arguments[arguments.length - 1];
            if (lastArgument instanceof Throwable) {
                Object[] messageArguments = new Object[arguments.length - 1];
                System.arraycopy(arguments, 0, messageArguments, 0, arguments.length - 1);
                return new Error(Strings.format(pattern, messageArguments), (Throwable) lastArgument);
            }
        }
        return new Error(Strings.format(pattern, arguments));
    }
}
