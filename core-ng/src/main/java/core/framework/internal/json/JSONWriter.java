package core.framework.internal.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import core.framework.util.Strings;

import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author neo
 */
public final class JSONWriter<T> {
    private static Map<Class<?>, JSONWriter<?>> cache;

    @SuppressWarnings("unchecked")
    public static <T> JSONWriter<T> of(Class<T> beanClass) {
        if (cache == null) cache = new HashMap<>();
        return (JSONWriter<T>) cache.computeIfAbsent(beanClass, JSONWriter::new);
    }

    public static void clearCache() {
        cache = null;
    }

    private final ObjectWriter writer;

    private JSONWriter(Class<T> instanceClass) {
        this.writer = JSONMapper.OBJECT_MAPPER.writerFor(instanceClass);
    }

    // with jdk 11, write to String then covert to byte[] is faster than write to byte[]
    // toJSON won't throw exception especially instance class will be validated before
    public byte[] toJSON(T instance) {
        try {
            return Strings.bytes(writer.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
