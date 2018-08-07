package core.framework.impl.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public class PartialJSONWriter<T> {
    private static final ObjectMapper OBJECT_MAPPER = JSONMapper.createObjectMapper(false);

    public static <T> PartialJSONWriter<T> of(Type instanceType) {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        return new PartialJSONWriter<>(OBJECT_MAPPER.writerFor(type));
    }

    private final ObjectWriter writer;

    private PartialJSONWriter(ObjectWriter writer) {
        this.writer = writer;
    }

    public byte[] toJSON(T instance) {
        try {
            return writer.writeValueAsBytes(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
