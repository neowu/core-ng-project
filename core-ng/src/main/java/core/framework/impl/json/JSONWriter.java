package core.framework.impl.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectWriter;
import core.framework.json.JSON;
import core.framework.util.Strings;

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
        JavaType type = JSON.OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        return new JSONWriter<>(JSON.OBJECT_MAPPER.writerFor(type));
    }

    private final ObjectWriter writer;

    private JSONWriter(ObjectWriter writer) {
        this.writer = writer;
    }

    // with jdk 11, write to String then covert to byte[] is faster than write to byte[]
    public byte[] toJSON(T instance) {
        try {
            return Strings.bytes(writer.writeValueAsString(instance));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
