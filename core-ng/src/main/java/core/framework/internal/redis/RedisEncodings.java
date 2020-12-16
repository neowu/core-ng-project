package core.framework.internal.redis;

import core.framework.util.Strings;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    static void validate(String name, String value) {
        if (value == null) throw new Error(name + " must not be null");
    }

    static void validate(String name, String... values) {
        if (values.length == 0) throw new Error(name + " must not be empty");
        for (String value : values) {
            if (value == null) throw new Error(name + " must not contain null");
        }
    }

    static void validate(String name, Map<?, ?> values) {
        if (values.isEmpty()) throw new Error(name + " must not be empty");
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) throw new Error(name + " must not contain null");
        }
    }

    static byte[] encode(String value) {
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
