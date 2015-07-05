package core.framework.api.util;

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

    public static Error error(String pattern, Object... arguments) {
        return new Error(Strings.format(pattern, arguments));
    }
}
