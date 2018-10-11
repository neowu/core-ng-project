package core.framework.internal.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import core.framework.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public class JSONMapper<T> {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new AfterburnerModule().setUseValueClassLoader(false));   // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
        mapper.setDateFormat(new StdDateFormat());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setAnnotationIntrospector(new JSONAnnotationIntrospector());
        return mapper;
    }

    // used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
    // refer to https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
    private final ObjectReader reader;
    private final ObjectWriter writer;

    public JSONMapper(Type instanceType) {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        reader = OBJECT_MAPPER.readerFor(type);
        writer = OBJECT_MAPPER.writerFor(type);
    }

    public T fromJSON(byte[] json) {
        try {
            return reader.readValue(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
