package core.framework.impl.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * used internally, performance is top priority in design
 *
 * @author neo
 */
public final class JSONWriter<T> {
    public static <T> JSONWriter<T> of(Type instanceType) {
        ObjectMapper objectMapper = JSONMapper.OBJECT_MAPPER;
        JavaType type = objectMapper.getTypeFactory().constructType(instanceType);
        return new JSONWriter<>(objectMapper.writerFor(type));
    }

    private final ObjectWriter writer;

    public JSONWriter(ObjectWriter writer) {
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
