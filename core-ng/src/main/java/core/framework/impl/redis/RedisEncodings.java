package core.framework.impl.redis;

import core.framework.util.Strings;

import java.nio.charset.StandardCharsets;

/**
 * @author neo
 */
class RedisEncodings {
    private static final byte[][] INT_BYTES_CACHE = new byte[256][];

    static {
        for (int i = 0; i < 256; i++) {
            String text = Integer.toString(i);
            INT_BYTES_CACHE[i] = Strings.bytes(text);
        }
    }

    static byte[] encode(String value) {
        if (value == null) throw new Error("value must not be null");
        return Strings.bytes(value);
    }

    static byte[] encode(long value) {
        if (value >= 0 && value < 256) {
            return INT_BYTES_CACHE[(int) value];
        }
        String text = Long.toString(value);
        return Strings.bytes(text); // according to JMH benchmark, text.getBytes(UTF_8) beats getBytesWithOtherCharset or convert by char[] directly, refer to JDK impl for details
    }

    static String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, StandardCharsets.UTF_8);
    }
}
