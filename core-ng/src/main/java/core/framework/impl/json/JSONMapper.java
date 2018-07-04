package core.framework.impl.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * used internally, performance is top priority in design
 *
 * @author neo
 */
public final class JSONMapper {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new AfterburnerModule().setUseValueClassLoader(false));   // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
        mapper.setDateFormat(new StdDateFormat());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
        mapper.setAnnotationIntrospector(new JSONAnnotationIntrospector());
        return mapper;
    }

    public static <T> T fromJSON(Type instanceType, byte[] json) {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] toJSON(Object instance) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
