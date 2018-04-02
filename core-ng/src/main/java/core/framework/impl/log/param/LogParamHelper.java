package core.framework.impl.log.param;

import java.nio.charset.Charset;

/**
 * @author neo
 */
class LogParamHelper {
    static final int MAX_LONG_STRING_SIZE = 30000; // limit long string to 30k

    static String toString(byte[] bytes, Charset charset, int maxSize) {
        if (bytes == null) return "null";
        if (bytes.length <= maxSize) return new String(bytes, charset);
        StringBuilder builder = new StringBuilder(maxSize + 14);
        String value = new String(bytes, 0, maxSize, charset);
        builder.append(value, 0, value.length() - 1)   // remove the last incomplete char, in utf8, one char takes 1 to 3 bytes
               .append("...(truncated)");   // length = 14
        return builder.toString();
    }
}
