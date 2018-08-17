package core.framework.impl.redis;

import core.framework.util.Charsets;
import core.framework.util.Strings;

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

    static byte[] encode(String value) {
        if (value == null) throw new RedisException("value must not be null");
        return Strings.bytes(value);
    }

    static byte[] encode(long value) {
        if (value >= 0 && value < 256) {
            return INT_BYTES_CACHE[(int) value];
        }
        String text = Long.toString(value);
        return Strings.bytes(text);
    }

    static byte[][] encode(String key, String... values) {
        if (values.length == 0) throw new RedisException("values must not be empty");
        int length = values.length;
        byte[][] result = new byte[length + (key == null ? 0 : 1)][];
        int index = 0;
        if (key != null)
            result[index++] = encode(key);
        for (String value : values)
            result[index++] = encode(value);
        return result;
    }

    static byte[][] encode(String key, Map<String, String> values) {
        if (values.isEmpty()) throw new RedisException("values must not be empty");
        byte[][] result = new byte[values.size() * 2 + (key == null ? 0 : 1)][];
        int index = 0;
        if (key != null)
            result[index++] = encode(key);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result[index++] = encode(entry.getKey());
            result[index++] = encode(entry.getValue());
        }
        return result;
    }

    static String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }
}
