package core.framework.impl.log;

import core.framework.api.util.Charsets;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author neo
 */
public final class LogParam {  // used to hold bytes in log message, only eval on flush, to save memory
    private static final int MAX_LONG_STRING_SIZE = 30000; // limit long string to 30k

    public static Object of(byte[] bytes) {
        return of(bytes, Charsets.UTF_8);
    }

    public static Object of(byte[] bytes, Charset charset) {
        return new StringParam(bytes, charset, MAX_LONG_STRING_SIZE);
    }

    public static Object of(Map<?, ?> map) {
        return new MapParam(map);
    }

    static class StringParam {
        private final byte[] bytes;
        private final Charset charset;
        private final int maxSize;

        StringParam(byte[] bytes, Charset charset, int maxSize) {
            this.bytes = bytes;
            this.charset = charset;
            this.maxSize = maxSize;
        }

        @Override
        public String toString() {
            if (bytes == null) return null;
            if (bytes.length <= maxSize) return new String(bytes, charset);
            StringBuilder builder = new StringBuilder(maxSize + 10);
            String value = new String(bytes, 0, maxSize, charset);
            builder.append(value, 0, value.length() - 1);   // remove the last incomplete char, in utf8, one char takes 1 to 3 bytes
            builder.append("...(truncated)");
            return builder.toString();
        }
    }

    static class MapParam {
        private final Map<?, ?> map;

        MapParam(Map<?, ?> map) {
            this.map = map;
        }

        // replicate AbstractMap.toString with encoding
        @Override
        public String toString() {
            if (map == null) return null;
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
            if (value instanceof byte[]) return new String((byte[]) value, Charsets.UTF_8);
            return String.valueOf(value);
        }
    }
}
