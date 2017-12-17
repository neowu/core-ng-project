package core.framework.impl.log;

import core.framework.util.Charsets;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author neo
 */
public final class LogParam {  // used to hold bytes in log message, only eval on flush, to save memory
    static final int MAX_LONG_STRING_SIZE = 30000; // limit long string to 30k

    public static Object of(byte[] bytes) {
        return of(bytes, Charsets.UTF_8);
    }

    public static Object of(byte[] bytes, Charset charset) {
        return new BytesParam(bytes, charset);
    }

    public static Object of(Map<?, ?> map) {
        return new MapParam(map);
    }

    static String toString(byte[] bytes, Charset charset, int maxSize) {
        if (bytes == null) return "null";
        if (bytes.length <= maxSize) return new String(bytes, charset);
        StringBuilder builder = new StringBuilder(maxSize + 14);
        String value = new String(bytes, 0, maxSize, charset);
        builder.append(value, 0, value.length() - 1)   // remove the last incomplete char, in utf8, one char takes 1 to 3 bytes
               .append("...(truncated)");   // length = 14
        return builder.toString();
    }

    private static class BytesParam {
        private final byte[] bytes;
        private final Charset charset;

        BytesParam(byte[] bytes, Charset charset) {
            this.bytes = bytes;
            this.charset = charset;
        }

        @Override
        public String toString() {
            return LogParam.toString(bytes, charset, MAX_LONG_STRING_SIZE);
        }
    }

    private static class MapParam {
        private final Map<?, ?> map;

        MapParam(Map<?, ?> map) {
            this.map = map;
        }

        // replicate AbstractMap.toString with encoding
        @Override
        public String toString() {
            if (map == null) return "null";
            StringBuilder builder = new StringBuilder();
            int index = 0;
            builder.append('{');
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (index > 0) builder.append(", ");
                Object key = entry.getKey();
                Object value = entry.getValue();
                builder.append(encode(key));
                builder.append('=');
                builder.append(encode(value));
                index++;
            }
            return builder.append('}').toString();
        }

        private String encode(Object value) {
            if (value instanceof byte[]) return LogParam.toString((byte[]) value, Charsets.UTF_8, MAX_LONG_STRING_SIZE);
            return String.valueOf(value);
        }
    }
}
