package core.framework.internal.json;

import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author neo
 */
public final class JSONReader<T> {
    private static Map<Class<?>, JSONReader<?>> cache;

    @SuppressWarnings("unchecked")
    public static <T> JSONReader<T> of(Class<T> beanClass) {
        synchronized (JSONReader.class) {
            if (cache == null) cache = new HashMap<>();
            return (JSONReader<T>) cache.computeIfAbsent(beanClass, JSONReader::new);
        }
    }

    public static void clearCache() {
        synchronized (JSONReader.class) {
            cache = null;
        }
    }

    // used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
    // refer to https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
    private final ObjectReader reader;

    private JSONReader(Class<T> instanceClass) {
        this.reader = JSONMapper.OBJECT_MAPPER.readerFor(instanceClass);
    }

    public T fromJSON(byte[] json) throws IOException {
        return reader.readValue(json);
    }

    public T fromJSON(String json) throws IOException {
        return reader.readValue(json);
    }
}
