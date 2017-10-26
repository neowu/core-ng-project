package core.framework.impl.redis;

import core.framework.util.Charsets;
import core.framework.util.Strings;

/**
 * @author neo
 */
class RedisEncodings {
    private static final byte[][] INT_BYTES_CACHE = new byte[256][];

    static {
        for (int i = 0; i < 256; i++) {
            String text = Integer.toString(i);
            INT_BYTES_CACHE[i] = bytes(text);
        }
    }

    static byte[] encode(String value) {
        if (value == null) throw new RedisException("value must not be null");
        return Strings.bytes(value);
    }

    static byte[] encode(long value) {
        if (value >= 0 && value <= 255) {
            return INT_BYTES_CACHE[(int) value];
        }
        String text = Long.toString(value);
        return bytes(text);
    }

    static byte[] encode(int value) {
        if (value >= 0 && value <= 255) {
            return INT_BYTES_CACHE[value];
        }
        String text = Integer.toString(value);
        return bytes(text);
    }

    static String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }

    private static byte[] bytes(String text) {
        char[] chars = text.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }
}
