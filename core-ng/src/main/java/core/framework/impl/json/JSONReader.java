package core.framework.impl.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
 *
 * @author neo
 */
public final class JSONReader<T> {
    public static <T> JSONReader<T> of(Type instanceType) {
        JavaType type = JSONMapper.OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        return new JSONReader<>(JSONMapper.OBJECT_MAPPER.readerFor(type));
    }

    private final ObjectReader reader;

    private JSONReader(ObjectReader reader) {
        this.reader = reader;
    }

    public T fromJSON(byte[] json) {
        try {
            return reader.readValue(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
